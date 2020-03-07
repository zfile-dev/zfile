package im.zhaojun.minio;

import cn.hutool.core.convert.Convert;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractS3FileService;
import im.zhaojun.common.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
public class MinIOServiceImpl extends AbstractS3FileService implements FileService {

    private static final Logger log = LoggerFactory.getLogger(MinIOServiceImpl.class);

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(getStorageTypeEnum());
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

                isInitialized = testConnection();
            }
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常, 已跳过");
        }
    }



    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.MINIO;
    }

}
