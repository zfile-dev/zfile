package im.zhaojun.huawei.service;

import cn.hutool.core.util.URLUtil;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HuaweiService implements FileService {

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

    @Override
    public void initMethod() {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.HUAWEI);
        String accessKey = stringStorageConfigMap.get(ACCESS_KEY).getValue();
        String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
        String endPoint = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();

        bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
        domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();
        obsClient = new ObsClient(accessKey, secretKey, endPoint);
    }

    @Override
    public List<FileItem> fileList(String path) {
        path = StringUtils.removeFirstSeparator(path);

        List<FileItem> fileItemList = new ArrayList<>();

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setDelimiter("/");
        listObjectsRequest.setPrefix(path);
        ObjectListing objectListing = obsClient.listObjects(listObjectsRequest);
        List<ObsObject> objects = objectListing.getObjects();

        for (ObsObject object : objects) {
            String fileName = object.getObjectKey();
            ObjectMetadata metadata = object.getMetadata();

            FileItem fileItem = new FileItem();
            fileItem.setName(fileName.substring(path.length()));
            fileItem.setSize(metadata.getContentLength());
            fileItem.setTime(metadata.getLastModified());
            fileItem.setType(FileTypeEnum.FILE);
            fileItemList.add(fileItem);
        }

        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            FileItem fileItem = new FileItem();
            fileItem.setName(commonPrefix.substring(0, commonPrefix.length() - 1));
            fileItem.setType(FileTypeEnum.FOLDER);
            fileItemList.add(fileItem);
        }

        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) throws Exception {
        path = StringUtils.removeFirstSeparator(path);
        TemporarySignatureRequest req = new TemporarySignatureRequest(HttpMethodEnum.GET, timeout);
        req.setBucketName(bucketName);
        req.setObjectKey(path);
        TemporarySignatureResponse res = obsClient.createTemporarySignature(req);
        URL url = new URL(res.getSignedUrl());
        return URLUtil.complateUrl(domain, url.getFile());
    }
}
