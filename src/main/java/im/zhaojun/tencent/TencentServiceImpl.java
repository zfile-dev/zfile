package im.zhaojun.tencent;

import cn.hutool.core.util.URLUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;
import im.zhaojun.common.config.ZFileCacheConfiguration;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public class TencentServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(TencentServiceImpl.class);

    @Resource
    private StorageConfigService storageConfigService;

    private static final String BUCKET_NAME_KEY = "bucket-name";

    private static final String SECRET_ID_KEY = "secretId";

    private static final String SECRET_KEY = "secretKey";

    private static final String DOMAIN_KEY = "domain";

    private static final String ENDPOINT_KEY = "endPoint";

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    private String bucketName;

    private String domain;

    private COSClient cosClient;

    private boolean isInitialized;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.TENCENT);
            String secretId = stringStorageConfigMap.get(SECRET_ID_KEY).getValue();
            String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
            String regionName = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();
            bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
            domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();

            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            Region region = new Region(regionName);
            ClientConfig clientConfig = new ClientConfig(region);
            cosClient = new COSClient(cred, clientConfig);
            isInitialized = true;
        } catch (Exception e) {
            log.debug(StorageTypeEnum.TENCENT.getDescription() + "初始化异常, 已跳过");
        }
    }

    @Cacheable
    @Override
    public List<FileItemDTO> fileList(String path) {
        path = StringUtils.removeFirstSeparator(path);

        List<FileItemDTO> fileItemList = new ArrayList<>();
        ObjectListing objectListing = cosClient.listObjects(new ListObjectsRequest().withBucketName(bucketName).withDelimiter("/").withPrefix(path));
        for (COSObjectSummary s : objectListing.getObjectSummaries()) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setName(s.getKey().substring(path.length()));
            fileItemDTO.setSize(s.getSize());
            fileItemDTO.setTime(s.getLastModified());
            fileItemDTO.setType(FileTypeEnum.FILE);
            fileItemDTO.setPath(path);
            fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, fileItemDTO.getName())));
            fileItemList.add(fileItemDTO);
        }

        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setName(commonPrefix.substring(path.length(), commonPrefix.length() - 1));
            fileItemDTO.setType(FileTypeEnum.FOLDER);
            fileItemDTO.setPath(path);
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    @Cacheable
    @Override
    public String getDownloadUrl(String path) {
        Date expirationDate = new Date(System.currentTimeMillis() + timeout * 1000);
        URL url = cosClient.generatePresignedUrl(bucketName, path, expirationDate);
        return URLUtil.complateUrl(domain, url.getFile());
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.TENCENT;
    }


    @Override
    public boolean getIsInitialized() {
        return isInitialized;
    }


}