package im.zhaojun.ftp.service;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.ftp.Ftp;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
public class FtpServiceImpl extends AbstractFileService implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FtpServiceImpl.class);

    @Resource
    private StorageConfigService storageConfigService;

    private Ftp ftp;

    private String domain;

    private String host;

    private String port;

    private String username;

    private String password;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(getStorageTypeEnum());
            host = stringStorageConfigMap.get(StorageConfigConstant.HOST_KEY).getValue();
            port = stringStorageConfigMap.get(StorageConfigConstant.PORT_KEY).getValue();
            username = stringStorageConfigMap.get(StorageConfigConstant.USERNAME_KEY).getValue();
            password = stringStorageConfigMap.get(StorageConfigConstant.PASSWORD_KEY).getValue();
            domain = stringStorageConfigMap.get(StorageConfigConstant.DOMAIN_KEY).getValue();
            super.basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();;
            if (Objects.isNull(host) || Objects.isNull(port) || Objects.isNull(username) || Objects.isNull(password)) {
                isInitialized = false;
            } else {
                ftp = new Ftp(host, Integer.parseInt(port), username, password, StandardCharsets.UTF_8);
                ftp.getClient().configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
                ftp.getClient().type(FTP.BINARY_FILE_TYPE);
                isInitialized = testConnection();
            }
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常, 已跳过");
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
    protected boolean testConnection() {
        // FTPClient ftpClient = ftp.getClient();
        // try {
        //     ftpClient.connect(host, Integer.parseInt(port));
        //     return ftpClient.login(username, password);
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return false;
        // }
        return true;
    }

    private void ifDisConnectionReConnection() {
        FTPClient ftpClient = ftp.getClient();
        try {
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();

            // ftpClient.reinitialize();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("断开了连接, 已尝试重新连接.");
                ftpClient.connect(host, Integer.parseInt(port));
                ftpClient.login(username, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
