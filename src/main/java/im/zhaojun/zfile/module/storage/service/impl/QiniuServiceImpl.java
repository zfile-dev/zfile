package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.QiniuParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class QiniuServiceImpl extends AbstractS3BaseFileService<QiniuParam> {

    private BucketManager bucketManager;

    private Auth auth;

    @Override
    public void init() {
        String endPoint = param.getEndPoint();
        String endPointScheme = param.getEndPointScheme();
        // 如果 endPoint 不包含协议部分, 且配置了 endPointScheme, 则手动拼接协议部分.
        if (!UrlUtils.hasScheme(endPoint) && StringUtils.isNotBlank(endPointScheme)) {
            endPoint = endPointScheme + "://" + endPoint;
        }

        software.amazon.awssdk.regions.Region oss = software.amazon.awssdk.regions.Region.of("kodo");
        URI endpointOverride = URI.create(endPoint);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(param.getAccessKey(), param.getSecretKey()));

        super.s3ClientNew = S3Client.builder()
                .overrideConfiguration(getClientConfiguration())
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(credentialsProvider)
                .build();

        super.s3Presigner = S3Presigner.builder()
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(credentialsProvider)
                .build();


        Configuration cfg = new Configuration(Region.autoRegion());
        auth = Auth.create(param.getAccessKey(), param.getSecretKey());
        bucketManager = new BucketManager(auth, cfg);

        setUploadCors();
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.QINIU;
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        String bucketName = param.getBucketName();
        String basePath = param.getBasePath();

        String srcPath = StringUtils.concat(basePath, getCurrentUserBasePath(), path, name);
        srcPath = StringUtils.trimStartSlashes(srcPath);

        String distPath = StringUtils.concat(basePath, getCurrentUserBasePath(), path, newName);
        distPath = StringUtils.trimStartSlashes(distPath);

        try {
            bucketManager.move(bucketName, srcPath, bucketName, distPath);
            return true;
        } catch (QiniuException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }

    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getDomain())) {
            return getProxyDownloadUrl(pathAndName, false);
        }
        String domain = param.getDomain();

        Integer tokenTime = param.getTokenTime();
        if (param.getTokenTime() == null || param.getTokenTime() < 1) {
            tokenTime = 1800;
        }

        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), pathAndName);
        // 如果不是私有空间, 且指定了加速域名, 则使用 qiniu 的 sdk 获取下载链接
        // (使用 s3 sdk 获取到的下载链接替换自动加速域名后无法访问, 故这里使用 qiniu sdk).
        if (BooleanUtils.isTrue(param.isPrivate()) && StringUtils.isNotEmpty(domain)) {
            String customDomainFullPath = StringUtils.removeDuplicateSlashes(domain + StringUtils.SLASH + StringUtils.encodeAllIgnoreSlashes(fullPath));
            return auth.privateDownloadUrl(customDomainFullPath, tokenTime);
        }

        return super.getDownloadUrl(pathAndName);
    }
}