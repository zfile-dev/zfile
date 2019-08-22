package im.zhaojun.aliyun.service;

import cn.hutool.core.util.URLUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AliyunService implements FileService {

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

    @Override
    public void initMethod() {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.ALIYUN);
        String accessKey = stringStorageConfigMap.get(ACCESS_KEY).getValue();
        String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
        String endPoint = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();

        bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
        domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();
        ossClient = new OSSClientBuilder().build(endPoint, accessKey, secretKey);

        AccessControlList bucketAcl = ossClient.getBucketAcl(bucketName);
        CannedAccessControlList cannedACL = bucketAcl.getCannedACL();
        isPrivate = "Private".equals(cannedACL.name());
    }

    @Override
    public List<FileItem> fileList(String path) {
        path = StringUtils.removeFirstSeparator(path);

        List<FileItem> fileItemList = new ArrayList<>();
        ObjectListing objectListing =
                ossClient.listObjects(new ListObjectsRequest(bucketName).withDelimiter("/").withPrefix(path));

        for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
            FileItem fileItem = new FileItem();
            fileItem.setName(s.getKey().substring(path.length()));
            fileItem.setSize(s.getSize());
            fileItem.setTime(s.getLastModified());
            fileItem.setType(FileTypeEnum.FILE);
            fileItemList.add(fileItem);
        }

        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            FileItem fileItem = new FileItem();
            fileItem.setName(commonPrefix.substring(path.length(), commonPrefix.length() - 1));
            fileItem.setType(FileTypeEnum.FOLDER);
            fileItemList.add(fileItem);
        }

        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) throws Exception {
        path = StringUtils.removeFirstSeparator(path);

        if (isPrivate) {
            Date expirationDate = new Date(new Date().getTime() + timeout * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, path, expirationDate);
            return URLUtil.complateUrl(domain, url.getFile());
        } else {
            return URLUtil.complateUrl(domain, path);
        }
    }
}
