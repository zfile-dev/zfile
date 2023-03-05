package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.QiniuParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
        BasicAWSCredentials credentials = new BasicAWSCredentials(param.getAccessKey(), param.getSecretKey());
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(param.getEndPoint(), "kodo")).build();

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

        String srcPath = StringUtils.concat(basePath, path, name);
        srcPath = StringUtils.trimStartSlashes(srcPath);

        String distPath = StringUtils.concat(basePath, path, newName);
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
        String domain = param.getDomain();

        Integer tokenTime = param.getTokenTime();
        if (param.getTokenTime() == null || param.getTokenTime() < 1) {
            tokenTime = 1800;
        }

        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath() + pathAndName);
        // 如果不是私有空间, 且指定了加速域名, 则使用 qiniu 的 sdk 获取下载链接
        // (使用 s3 sdk 获取到的下载链接替换自动加速域名后无法访问, 故这里使用 qiniu sdk).
        if (BooleanUtil.isTrue(param.isPrivate()) && StrUtil.isNotEmpty(domain)) {
            String customDomainFullPath = StringUtils.removeDuplicateSlashes(domain + "/" + StringUtils.encodeAllIgnoreSlashes(fullPath));
            return auth.privateDownloadUrl(customDomainFullPath, tokenTime);
        }

        return super.getDownloadUrl(pathAndName);
    }
}