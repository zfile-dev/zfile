package im.zhaojun.tencent;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import im.zhaojun.common.config.ZFileCacheConfiguration;
import im.zhaojun.common.model.S3Model;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractS3FileService;
import im.zhaojun.common.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public class TencentServiceImpl extends AbstractS3FileService implements FileService {

    private static final Logger log = LoggerFactory.getLogger(TencentServiceImpl.class);

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.TENCENT);
            String secretId = stringStorageConfigMap.get(StorageConfigConstant.SECRET_ID_KEY).getValue();
            String secretKey = stringStorageConfigMap.get(StorageConfigConstant.SECRET_KEY).getValue();
            String regionName = stringStorageConfigMap.get(StorageConfigConstant.ENDPOINT_KEY).getValue();
            String bucketName = stringStorageConfigMap.get(StorageConfigConstant.BUCKET_NAME_KEY).getValue();
            String domain = stringStorageConfigMap.get(StorageConfigConstant.DOMAIN_KEY).getValue();
            basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();

            BasicAWSCredentials credentials = new BasicAWSCredentials(secretId, secretKey);
            s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(regionName, "cos")).build();

            s3Model = S3Model.builder().bucketName(bucketName).domain(domain).build();

            isInitialized = testConnection();
        } catch (Exception e) {
            log.debug(StorageTypeEnum.TENCENT.getDescription() + "初始化异常, 已跳过");
        }
    }

    @Override
    @Cacheable
    public List<FileItemDTO> fileList(String path) throws Exception {
        s3Model.setPath(path);
        return s3FileList(s3Client, s3Model);
    }

    @Override
    @Cacheable
    public String getDownloadUrl(String path) {
        s3Model.setPath(path);
        return s3ObjectUrl(s3Client, s3Model);
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.TENCENT;
    }

}