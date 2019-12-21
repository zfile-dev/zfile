package im.zhaojun.minio;

import im.zhaojun.common.config.ZFileCacheConfiguration;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public class MinIOServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(MinIOServiceImpl.class);

    private String bucketName;

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    private static final String BUCKET_NAME_KEY = "bucket-name";

    private static final String ACCESS_KEY = "accessKey";

    private static final String SECRET_KEY = "secretKey";

    private static final String ENDPOINT_KEY = "endPoint";

    private static final String BASE_PATH = "base-path";

    @Resource
    private StorageConfigService storageConfigService;

    private MinioClient minioClient;

    private boolean isInitialized;

    private String basePath;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.MINIO);
            String accessKey = stringStorageConfigMap.get(ACCESS_KEY).getValue();
            String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
            String endPoint = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();
            bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
            minioClient = new MinioClient(endPoint, accessKey, secretKey);
            basePath = stringStorageConfigMap.get(BASE_PATH).getValue();
            basePath = basePath == null ? "" : basePath;
            isInitialized = true;
        } catch (Exception e) {
            log.debug(StorageTypeEnum.MINIO.getDescription() + "初始化异常, 已跳过");
        }
    }

    @Cacheable
    @Override
    public List<FileItemDTO> fileList(String path) throws Exception {
        path = StringUtils.removeFirstSeparator(path);
        String fullPath = StringUtils.removeFirstSeparator(StringUtils.removeDuplicateSeparator(basePath + "/" +  path + "/"));
        List<FileItemDTO> fileItemList = new ArrayList<>();

        Iterable<Result<Item>> iterable = minioClient.listObjects(bucketName, fullPath, false);

        for (Result<Item> itemResult : iterable) {
            Item item = itemResult.get();

            FileItemDTO fileItemDTO = new FileItemDTO();
            if (item.isDir()) {
                fileItemDTO.setName(StringUtils.removeLastSeparator(item.objectName().replace(fullPath, "")));
                fileItemDTO.setType(FileTypeEnum.FOLDER);
                fileItemDTO.setPath(path);
            } else {
                fileItemDTO.setName(item.objectName().replace(fullPath, ""));
                fileItemDTO.setSize(item.objectSize());
                fileItemDTO.setTime(item.lastModified());
                fileItemDTO.setType(FileTypeEnum.FILE);
                fileItemDTO.setPath(path);
                fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(fullPath, fileItemDTO.getName())));
            }
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    @Cacheable
    @Override
    public String getDownloadUrl(String path) throws Exception {
        return minioClient.presignedGetObject(bucketName, path, timeout.intValue());
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.MINIO;
    }

    @Override
    public boolean getIsInitialized() {
        return isInitialized;
    }
}
