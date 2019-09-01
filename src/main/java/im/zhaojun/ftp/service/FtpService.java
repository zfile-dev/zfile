package im.zhaojun.ftp.service;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.ftp.Ftp;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FtpService implements FileService {

    @Resource
    private StorageConfigService storageConfigService;

    private static final String HOST_KEY = "host";

    private static final String PORT_KEY = "port";

    private static final String USERNAME_KEY = "username";

    private static final String PASSWORD_KEY = "password";

    private static final String DOMAIN_KEY = "domain";

    private Ftp ftp;

    private String domain;

    @Override
    public void initMethod() {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.FTP);
        String host = stringStorageConfigMap.get(HOST_KEY).getValue();
        String port = stringStorageConfigMap.get(PORT_KEY).getValue();
        String username = stringStorageConfigMap.get(USERNAME_KEY).getValue();
        String password = stringStorageConfigMap.get(PASSWORD_KEY).getValue();
        domain = stringStorageConfigMap.get(DOMAIN_KEY).getValue();

        ftp = new Ftp(host, Integer.parseInt(port), username, password);
    }

    @Override
    public List<FileItem> fileList(String path) {
        FTPFile[] ftpFiles = ftp.lsFiles(path);

        List<FileItem> fileItemList = new ArrayList<>();

        for (FTPFile ftpFile : ftpFiles) {
            FileItem fileItem = new FileItem();
            fileItem.setName(ftpFile.getName());
            fileItem.setSize(ftpFile.getSize());
            fileItem.setTime(ftpFile.getTimestamp().getTime());
            fileItem.setType(ftpFile.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
            fileItem.setPath(path);
            fileItemList.add(fileItem);
        }
        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) {
        return URLUtil.complateUrl(domain, path);
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.FTP;
    }
}
