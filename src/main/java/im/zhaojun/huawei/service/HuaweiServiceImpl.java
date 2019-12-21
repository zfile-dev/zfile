package im.zhaojun.huawei.service;

import cn.hutool.core.util.URLUtil;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
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
import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@CacheConfig(cacheNames = ZFileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public class HuaweiServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(HuaweiServiceImpl.class);

    private String bucketName;

    private String domain;

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    private static final String BUCKET_NAME_KEY = "bucket-name";

    private static final String ACCESS_KEY = "accessKey";

    private static final String SECRET_KEY = "secretKey";

    private static final String DOMAIN_KEY = "domain";

    private static final String ENDPOINT_KEY = "endPoint";

    @Resource
    private StorageConfigService storageConfigService;

    private ObsClient obsClient;

    private boolean isInitialized;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.HUAWEI);
            String accessKey = stringStorageConfigMap.get(ACCESS_KEY).getValue();
            String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
            String endPoint = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();

            bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
            domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();
            obsClient = new ObsClient(accessKey, secretKey, endPoint);
            isInitialized = true;
        } catch (Exception e) {
            log.debug(StorageTypeEnum.HUAWEI.getDescription() + "初始化异常, 已跳过");
        }
    }

    @Override
    @Cacheable
    public List<FileItemDTO> fileList(String path) throws Exception {
        path = StringUtils.removeFirstSeparator(path);

        List<FileItemDTO> fileItemList = new ArrayList<>();

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setDelimiter("/");
        listObjectsRequest.setPrefix(path);
        ObjectListing objectListing = obsClient.listObjects(listObjectsRequest);
        List<ObsObject> objects = objectListing.getObjects();

        for (ObsObject object : objects) {
            String fileName = object.getObjectKey();
            ObjectMetadata metadata = object.getMetadata();

            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setName(fileName.substring(path.length()));
            fileItemDTO.setSize(metadata.getContentLength());
            fileItemDTO.setTime(metadata.getLastModified());
            fileItemDTO.setType(FileTypeEnum.FILE);
            fileItemDTO.setPath(path);
            fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, fileItemDTO.getName())));
            fileItemList.add(fileItemDTO);
        }

        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setName(commonPrefix.substring(0, commonPrefix.length() - 1));
            fileItemDTO.setType(FileTypeEnum.FOLDER);
            fileItemDTO.setPath(path);
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    @Override
    @Cacheable
    public String getDownloadUrl(String path) throws Exception {
        path = StringUtils.removeFirstSeparator(path);
        TemporarySignatureRequest req = new TemporarySignatureRequest(HttpMethodEnum.GET, timeout);
        req.setBucketName(bucketName);
        req.setObjectKey(path);
        TemporarySignatureResponse res = obsClient.createTemporarySignature(req);
        URL url = new URL(res.getSignedUrl());
        return URLUtil.complateUrl(domain, url.getFile());
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.HUAWEI;
    }


    @Override
    public boolean getIsInitialized() {
        return isInitialized;
    }


}