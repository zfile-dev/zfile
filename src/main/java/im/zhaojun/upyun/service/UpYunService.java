package im.zhaojun.upyun.service;

import cn.hutool.core.util.URLUtil;
import com.UpYun;
import com.upyun.UpException;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UpYunService implements FileService {

    @Resource
    private StorageConfigService storageConfigService;

    private static final String BUCKET_NAME_KEY = "bucket-name";

    private static final String USERNAME_KEY = "username";

    private static final String PASSWORD_KEY = "password";

    private static final String DOMAIN_KEY = "domain";

    private String domain;

    private UpYun upYun;

    public void initMethod() {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.UPYUN);
        String bucketName = stringStorageConfigMap.get(BUCKET_NAME_KEY).getValue();
        String username = stringStorageConfigMap.get(USERNAME_KEY).getValue();
        String password = stringStorageConfigMap.get(PASSWORD_KEY).getValue();
        domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();
        upYun = new UpYun(bucketName, username, password);
    }

    public List<FileItem> fileList(String path) throws IOException, UpException {
        ArrayList<FileItem> fileItems = new ArrayList<>();

        List<UpYun.FolderItem> folderItems = upYun.readDir(path, null);

        if (folderItems != null) {
            for (UpYun.FolderItem folderItem : folderItems) {
                FileItem fileItem = new FileItem();
                fileItem.setName(folderItem.name);
                fileItem.setSize(folderItem.size);
                fileItem.setTime(folderItem.date);

                if ("Folder".equals(folderItem.type)) {
                    fileItem.setType(FileTypeEnum.FOLDER);
                } else if ("File".equals(folderItem.type)) {
                    fileItem.setType(FileTypeEnum.FILE);
                }
                fileItems.add(fileItem);
            }
        }
        return fileItems;
    }

    @Override
    public String getDownloadUrl(String path) {
        return URLUtil.complateUrl(domain, path);
    }

}
