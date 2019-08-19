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
public class TencentService implements FileService {

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

    @Override
    public void initMethod() {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.TENCENT);
        String secretId = stringStorageConfigMap.get(SECRET_ID_KEY).getValue();
        String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();
        String endPoint = stringStorageConfigMap.get(ENDPOINT_KEY).getValue();
        bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
        domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        Region region = new Region("ap-shanghai");
        ClientConfig clientConfig = new ClientConfig(region);
//        clientConfig.setSignExpired();
        cosClient = new COSClient(cred, clientConfig);
    }

    @Override
    public List<FileItem> fileList(String path) {
        path = StringUtils.removeFirstSeparator(path);

        List<FileItem> fileItemList = new ArrayList<>();
        ObjectListing objectListing = cosClient.listObjects(new ListObjectsRequest().withBucketName(bucketName).withDelimiter("/").withPrefix(path));
        for (COSObjectSummary s : objectListing.getObjectSummaries()) {
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
    public String getDownloadUrl(String path) {
        Date expirationDate = new Date(new Date().getTime() + timeout * 1000);
        URL url = cosClient.generatePresignedUrl(bucketName, path, expirationDate);
        return URLUtil.complateUrl(domain, url.getFile());
    }
}
