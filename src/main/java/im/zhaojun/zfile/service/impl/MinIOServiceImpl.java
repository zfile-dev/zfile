package im.zhaojun.zfile.service.impl;

import cn.hutool.core.convert.Convert;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.base.AbstractS3BaseFileService;
import im.zhaojun.zfile.service.base.BaseFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MinIOServiceImpl extends AbstractS3BaseFileService implements BaseFileService {

    private static final Logger log = LoggerFactory.getLogger(MinIOServiceImpl.class);

    @Override
    public void init(Integer driveId) {
        this.driveId = driveId;
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByDriveId(driveId);
        String accessKey = stringStorageConfigMap.get(StorageConfigConstant.ACCESS_KEY).getValue();
        String secretKey = stringStorageConfigMap.get(StorageConfigConstant.SECRET_KEY).getValue();
        String endPoint = stringStorageConfigMap.get(StorageConfigConstant.ENDPOINT_KEY).getValue();
        bucketName = stringStorageConfigMap.get(StorageConfigConstant.BUCKET_NAME_KEY).getValue();
        basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();
        isPrivate = Convert.toBool(stringStorageConfigMap.get(StorageConfigConstant.IS_PRIVATE).getValue(), true);

        if (Objects.isNull(accessKey) || Objects.isNull(secretKey) || Objects.isNull(endPoint) || Objects.isNull(bucketName)) {
            log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
            isInitialized = false;
        } else {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            s3Client = AmazonS3ClientBuilder.standard()
                    .withPathStyleAccessEnabled(true)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, "minio")).build();

            testConnection();
            isInitialized = true;
        }
    }



    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.MINIO;
    }

    @Override
    public List<StorageConfig> storageStrategyConfigList() {
        return new ArrayList<StorageConfig>() {{
            add(new StorageConfig("accessKey", "AccessKey"));
            add(new StorageConfig("secretKey", "SecretKey"));
            add(new StorageConfig("endPoint", "服务地址"));
            add(new StorageConfig("bucketName", "存储空间名称"));
            add(new StorageConfig("basePath", "基路径"));
            add(new StorageConfig("isPrivate", "是否是私有空间"));
        }};
    }

}