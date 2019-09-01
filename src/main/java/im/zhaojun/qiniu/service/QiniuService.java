package im.zhaojun.qiniu.service;

import cn.hutool.core.util.URLUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class QiniuService implements FileService {

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    @Resource
    private StorageConfigService storageConfigService;

    private static final String BUCKET_NAME_KEY = "bucket-name";

    private static final String ACCESS_KEY = "accessKey";

    private static final String SECRET_KEY = "secretKey";

    private static final String DOMAIN_KEY = "domain";

    private BucketManager bucketManager;

    private Auth auth;

    private String bucketName;

    private String domain;

    private boolean isPrivate;

    public void initMethod() throws QiniuException {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.QINIU);
        String accessKey = stringStorageConfigMap.get(ACCESS_KEY).getValue();
        String secretKey = stringStorageConfigMap.get(SECRET_KEY).getValue();

        Configuration cfg = new Configuration(Zone.autoZone());
        auth = Auth.create(accessKey, secretKey);
        bucketManager = new BucketManager(auth, cfg);
        bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
        domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();

        isPrivate = bucketManager.getBucketInfo(bucketName).getPrivate() == 1;
    }

    @Override
    public List<FileItem> fileList(String path) throws Exception {
        path = StringUtils.removeFirstSeparator(path);
        List<FileItem> fileItemList = new ArrayList<>();

        // 每次迭代的长度限制, 最大1000, 推荐值 1000
        int limit = 1000;
        // 指定目录分隔符, 列出所有公共前缀(模拟列出目录效果). 缺省值为空字符串
        String delimiter = "/";
        // 列举空间文件列表
        FileListing fileListing = bucketManager.listFilesV2(bucketName, path, "", limit, delimiter);
        for (FileInfo item : fileListing.items) {
            String fileKey = item.key;
            String fileName = fileKey.substring(path.length());

            FileItem fileItem = new FileItem();
            fileItem.setName(fileName);
            fileItem.setSize(item.fsize);
            fileItem.setTime(new Date(item.putTime / 1000));
            fileItem.setType(FileTypeEnum.FILE);
            fileItem.setPath(path);
            fileItemList.add(fileItem);
        }

        String[] commonPrefixes = fileListing.commonPrefixes;

        for (String commonPrefix : commonPrefixes) {
            FileItem fileItem = new FileItem();
            fileItem.setName(commonPrefix.substring(0, commonPrefix.length() - 1));
            fileItem.setType(FileTypeEnum.FOLDER);
            fileItem.setPath(path);
            fileItemList.add(fileItem);
        }

        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) {
        String url = URLUtil.complateUrl(domain, path);
        if (isPrivate) {
            url = auth.privateDownloadUrl(url, timeout);
        }
        return url;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.QINIU;
    }
}