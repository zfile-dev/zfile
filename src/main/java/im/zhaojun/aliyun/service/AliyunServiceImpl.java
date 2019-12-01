package im.zhaojun.aliyun.service;

import cn.hutool.core.util.URLUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AliyunServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(AliyunServiceImpl.class);

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    @Resource
    private StorageConfigService storageConfigService;

    private static final String BUCKET_NAME_KEY = "bucket-name";

    private static final String ACCESS_KEY = "accessKey";

    private static final String SECRET_KEY = "secretKey";

    private static final String DOMAIN_KEY = "domain";

    private static final String ENDPOINT_KEY = "endPoint";

    private OSS ossClient;

    private String bucketName;

    private String domain;

    private boolean isPrivate;

    private boolean isInitialized;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.ALIYUN);
            String accessKey = stringStorageConfigMap.get(ACCESS_KEY).getValue();
            String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
            String endPoint = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();

            bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
            domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();
            ossClient = new OSSClientBuilder().build(endPoint, accessKey, secretKey);

            AccessControlList bucketAcl = ossClient.getBucketAcl(bucketName);
            CannedAccessControlList cannedAcl = bucketAcl.getCannedACL();
            isPrivate = "Private".equals(cannedAcl.name());
            isInitialized = true;
        } catch (Exception e) {
            log.debug(StorageTypeEnum.ALIYUN.getDescription() + "初始化异常, 已跳过");
        }
    }

    @Override
    public List<FileItemDTO> fileList(String path) {
        path = StringUtils.removeFirstSeparator(path);

        List<FileItemDTO> fileItemList = new ArrayList<>();
        ObjectListing objectListing =
                ossClient.listObjects(new ListObjectsRequest(bucketName).withDelimiter("/").withPrefix(path));

        for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
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

    @Override
    public String getDownloadUrl(String path) {
        path = StringUtils.removeFirstSeparator(path);

        if (isPrivate) {
            Date expirationDate = new Date(System.currentTimeMillis() + timeout * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, path, expirationDate);
            return URLUtil.complateUrl(domain, url.getFile());
        } else {
            return URLUtil.complateUrl(domain, path);
        }
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.ALIYUN;
    }

    @Override
    public boolean getIsInitialized() {
        return isInitialized;
    }
}
