package im.zhaojun.zfile.home.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.admin.model.param.QiniuParam;
import im.zhaojun.zfile.home.service.base.AbstractS3BaseFileService;
import im.zhaojun.zfile.common.util.StringUtils;
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

    @Override
    public void init() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(param.getAccessKey(), param.getSecretKey());
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(param.getEndPoint(), "kodo")).build();

        Configuration cfg = new Configuration(Region.autoRegion());
        Auth auth = Auth.create(param.getAccessKey(), param.getSecretKey());
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
            log.error("存储源 {} 重命名文件 {} 至 {} 失败", storageId, srcPath, distPath, e);
        }

        return false;
    }

}