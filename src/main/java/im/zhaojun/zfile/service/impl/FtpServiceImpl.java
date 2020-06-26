package im.zhaojun.zfile.service.impl;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.ftp.Ftp;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.service.base.BaseFileService;
import im.zhaojun.zfile.util.StringUtils;
import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FtpServiceImpl extends AbstractBaseFileService implements BaseFileService {

    @Resource
    private StorageConfigService storageConfigService;

    private Ftp ftp;

    private String domain;

    private String host;

    private String port;

    private String username;

    private String password;

    @SneakyThrows(IOException.class)
    @Override
    public void init(Integer driveId) {
        this.driveId = driveId;
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByDriveId(driveId);
        host = stringStorageConfigMap.get(StorageConfigConstant.HOST_KEY).getValue();
        port = stringStorageConfigMap.get(StorageConfigConstant.PORT_KEY).getValue();
        username = stringStorageConfigMap.get(StorageConfigConstant.USERNAME_KEY).getValue();
        password = stringStorageConfigMap.get(StorageConfigConstant.PASSWORD_KEY).getValue();
        domain = stringStorageConfigMap.get(StorageConfigConstant.DOMAIN_KEY).getValue();
        super.basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();
        if (Objects.isNull(host) || Objects.isNull(port)) {
            isInitialized = false;
        } else {
            ftp = new Ftp(host, Integer.parseInt(port), username, password, StandardCharsets.UTF_8);
            ftp.getClient().configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
            ftp.getClient().type(FTP.BINARY_FILE_TYPE);
            testConnection();
            isInitialized = true;
        }
    }

    @Override
    public List<FileItemDTO> fileList(String path) {
        ftp.reconnectIfTimeout();
        String fullPath = StringUtils.getFullPath(basePath, path);
        ftp.cd(fullPath);
        FTPFile[] ftpFiles = new FTPFile[]{};
        try {
            ftp.getClient().changeWorkingDirectory("/");
            ftpFiles = ftp.getClient().listFiles(fullPath);
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        }

        List<FileItemDTO> fileItemList = new ArrayList<>();

        for (FTPFile ftpFile : ftpFiles) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setName(ftpFile.getName());
            fileItemDTO.setSize(ftpFile.getSize());
            fileItemDTO.setTime(ftpFile.getTimestamp().getTime());
            fileItemDTO.setType(ftpFile.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
            fileItemDTO.setPath(path);
            if (ftpFile.isFile()) {
                fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, fileItemDTO.getName())));
            }
            fileItemList.add(fileItemDTO);
        }
        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) {
        String fullPath = StringUtils.getFullPath(basePath, path);
        if (StringUtils.isNullOrEmpty(domain)) {
            return "ftp://"
                    + URLUtil.encodeQuery(username)
                    + ":"
                    + URLUtil.encodeQuery(password)
                    + "@"
                    + host + ":" + port + fullPath;
        }

        return URLUtil.complateUrl(domain, fullPath);
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.FTP;
    }

    @Override
    public FileItemDTO getFileItem(String path) {
        FileItemDTO fileItemDTO = new FileItemDTO();
        fileItemDTO.setUrl(getDownloadUrl(path));
        return fileItemDTO;
    }

    @Override
    public List<StorageConfig> storageStrategyConfigList() {
        return new ArrayList<StorageConfig>() {{
            add(new StorageConfig("host", "域名或IP"));
            add(new StorageConfig("port", "端口"));
            add(new StorageConfig("username", "用户名"));
            add(new StorageConfig("password", "密码"));
            add(new StorageConfig("domain", "加速域名"));
            add(new StorageConfig("basePath", "基路径"));
        }};
    }

}