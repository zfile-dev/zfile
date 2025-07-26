package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.ftp.Ftp;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.util.ArrayUtils;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.FtpParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import im.zhaojun.zfile.module.storage.support.ftp.FtpClientFactory;
import im.zhaojun.zfile.module.storage.support.ftp.FtpClientPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FtpServiceImpl extends AbstractProxyTransferService<FtpParam> {

    private FtpClientPool ftpClientPool;

    public static final String FTP_MODE_ACTIVE = "active";

    public static final String FTP_MODE_PASSIVE = "passive";

    @Override
    public void init() {
        Charset charset = Charset.forName(param.getEncoding());
        FtpClientFactory factory = new FtpClientFactory(param.getHost(), param.getPort(), param.getUsername(), param.getPassword(), charset, param.getFtpMode());
        GenericObjectPoolConfig<FtpClientFactory> config = new GenericObjectPoolConfig<>();
        config.setTestOnBorrow(true);
        config.setMaxWait(Duration.ofSeconds(15));
        ftpClientPool = new FtpClientPool(factory, config);
    }

    public Ftp getClientFromPool() {
        try {
            return ftpClientPool.borrowObject();
        } catch (NoSuchElementException e) {
            throw new BizException(ErrorCode.BIZ_FTP_CLIENT_POOL_FULL);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    @Override
    public List<FileItemResult> fileList(String folderPath) throws IOException {
        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath);
            FTPFile[] ftpFiles = ftp.lsFiles(fullPath);
            List<FileItemResult> fileItemList = new ArrayList<>();

            for (FTPFile ftpFile : ftpFiles) {
                // 跳过 ftp 的本目录和上级目录
                if (Arrays.asList(".", "..").contains(ftpFile.getName())) {
                    continue;
                }
                FileItemResult fileItemResult = ftpFileToFileItem(ftpFile, folderPath);
                fileItemList.add(fileItemResult);
            }
            return fileItemList;
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }


    @Override
    public FileItemResult getFileItem(String pathAndName) {
        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            String folderPath = FileUtils.getParentPath(pathAndName);
            String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath);
            FTPFile[] ftpFiles = ftp.lsFiles(fullPath);

            if (ArrayUtils.isEmpty(ftpFiles)) {
                return null;
            }

            String fileName = FileUtils.getName(pathAndName);

            for (FTPFile ftpFile : ftpFiles) {
                if (Objects.equals(ftpFile.getName(), fileName)) {
                    return ftpFileToFileItem(ftpFile, folderPath);
                }
            }
            return null;
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }


    @Override
    public boolean newFolder(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            return ftp.mkdir(fullPath);
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }


    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            return ftp.delFile(fullPath);
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }


    @Override
    public boolean deleteFolder(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            return ftp.delDir(fullPath);
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }


    @Override
    public boolean renameFile(String path, String name, String newName) {
        return moveFile(path, name, path, newName);
    }


    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }


    @Override
    public String getDownloadUrl(String pathAndName) {
        if (StringUtils.isNotBlank(param.getDomain())) {
            return StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(pathAndName));
        }
        return super.getProxyDownloadUrl(pathAndName);
    }



    @Override
    public ResponseEntity<Resource> downloadToStream(String pathAndName) throws IOException {
        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            // 如果配置了域名，还访问代理下载 URL, 则抛出异常进行提示.
            if (StringUtils.isNotEmpty(param.getDomain())) {
                throw new BizException(ErrorCode.BIZ_UNSUPPORTED_PROXY_DOWNLOAD);
            }

            pathAndName = StringUtils.concat(param.getBasePath(), pathAndName);
            String fileName = FileUtils.getName(pathAndName);
            Long fileSize = param.isEnableRange() ? Convert.toLong(ftp.getClient().getSize(pathAndName),0L) : null;

            InputStream inputStream = ftp.getClient().retrieveFileStream(pathAndName);
            RequestHolder.writeFile(inputStream, fileName, fileSize, false, param.isProxyLinkForceDownload());
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
        return null;
    }


    @Override
    public String getUploadUrl(String path, String name, Long size) {
        return super.getProxyUploadUrl(path, name);
    }


    @Override
    public void uploadFile(String pathAndName, InputStream inputStream, Long size) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), pathAndName);
        String fileName = FileUtils.getName(pathAndName);
        String folderName = FileUtils.getParentPath(fullPath);

        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            ftp.upload(folderName, fileName, inputStream);
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }

    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        throw new BizException(ErrorCode.BIZ_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        throw new BizException(ErrorCode.BIZ_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        String srcPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String distPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath, targetName);

        Ftp ftp = null;
        try {
            ftp = getClientFromPool();
            return ftp.getClient().rename(srcPath, distPath);
        } catch (IOException e) {
            throw new SystemException(e);
        } finally {
            if (ftp != null) {
                ftpClientPool.returnObject(ftp);
            }
        }
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        return moveFile(path, name, targetPath, targetName);
    }

    private FileItemResult ftpFileToFileItem(FTPFile ftpFile, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(ftpFile.getName());
        fileItemResult.setSize(ftpFile.getSize());
        fileItemResult.setTime(ftpFile.getTimestamp().getTime());
        fileItemResult.setType(ftpFile.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
        fileItemResult.setPath(folderPath);

        if (fileItemResult.getType() == FileTypeEnum.FILE) {
            fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName())));
        }
        return fileItemResult;
    }

    @Override
    public StorageSourceMetadata getStorageSourceMetadata() {
        StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
        storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
        return storageSourceMetadata;
    }

    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.FTP;
    }

    @Override
    public void destroy() {
        if (ftpClientPool != null) {
            ftpClientPool.close();
        }
    }

}