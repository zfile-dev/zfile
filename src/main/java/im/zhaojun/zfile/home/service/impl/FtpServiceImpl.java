package im.zhaojun.zfile.home.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import im.zhaojun.zfile.admin.model.param.FtpParam;
import im.zhaojun.zfile.common.exception.DisableProxyDownloadException;
import im.zhaojun.zfile.common.exception.file.operator.DownloadFileException;
import im.zhaojun.zfile.common.exception.file.operator.GetFileInfoException;
import im.zhaojun.zfile.common.util.RequestHolder;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import im.zhaojun.zfile.home.service.base.ProxyTransferService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FtpServiceImpl extends ProxyTransferService<FtpParam> {

    private Ftp ftp;

    @SneakyThrows(IOException.class)
    @Override
    public void init() {
        Charset charset = Charset.forName(param.getEncoding());
        ftp = new Ftp(param.getHost(), param.getPort(), param.getUsername(),
                        param.getPassword(), charset);
        ftp.getClient().type(FTP.BINARY_FILE_TYPE);
        ftp.setMode(FtpMode.Passive);
    }


    @Override
    public synchronized List<FileItemResult> fileList(String folderPath) {
        ftp.reconnectIfTimeout();
        String fullPath = StringUtils.concat(param.getBasePath(), folderPath);
        ftp.cd(fullPath);
        FTPFile[] ftpFiles = new FTPFile[]{};
        try {
            ftp.getClient().changeWorkingDirectory("/");
            ftpFiles = ftp.getClient().listFiles(fullPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    }


    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.FTP;
    }


    @Override
    public FileItemResult getFileItem(String pathAndName) {
        FTPFile[] ftpFiles;
        try {
            ftpFiles = ftp.getClient().listFiles(pathAndName);
        } catch (IOException e) {
            throw new GetFileInfoException(storageId, pathAndName, e);
        }

        if (ArrayUtil.isEmpty(ftpFiles)) {
            throw new GetFileInfoException(storageId, pathAndName);
        }

        FTPFile ftpFile = ftpFiles[0];

        String folderPath = StringUtils.getParentPath(pathAndName);
        return ftpFileToFileItem(ftpFile, folderPath);
    }


    @Override
    public synchronized boolean newFolder(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);
        return ftp.mkdir(fullPath);
    }


    @Override
    public synchronized boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);
        return ftp.delFile(fullPath);
    }


    @Override
    public synchronized boolean deleteFolder(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);
        return ftp.delDir(fullPath);
    }


    @Override
    public synchronized boolean renameFile(String path, String name, String newName) {
        ftp.reconnectIfTimeout();
        String srcPath = StringUtils.concat(param.getBasePath(), path, name);
        String distPath = StringUtils.concat(param.getBasePath(), path, newName);
        try {
            return ftp.getClient().rename(srcPath, distPath);
        } catch (IOException e) {
            log.error("存储源 {} 重命名文件 {} 至 {} 失败", storageId, srcPath, distPath, e);
        }

        return false;
    }


    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }


    @Override
    public synchronized ResponseEntity<Resource> downloadToStream(String pathAndName) {
        // 如果配置了域名，还访问代理下载 URL, 则抛出异常进行提示.
        if (StrUtil.isNotEmpty(param.getDomain())) {
            throw new DisableProxyDownloadException();
        }

        ftp.reconnectIfTimeout();
        HttpServletResponse response = RequestHolder.getResponse();
        try {
            pathAndName = StringUtils.concat(param.getBasePath(), pathAndName);
            OutputStream outputStream = response.getOutputStream();
            String fileName = FileUtil.getName(pathAndName);
            String folderName = FileUtil.getParent(pathAndName, 1);
            ftp.download(folderName, fileName, outputStream);
        } catch (Exception e) {
            throw new DownloadFileException(storageId, "下载文件失败", e);
        }
        return null;
    }


    @Override
    public synchronized void uploadFile(String path, InputStream inputStream) {
        String fullPath = StringUtils.concat(param.getBasePath(), path);
        String fileName = FileUtil.getName(path);
        String folderName = FileUtil.getParent(fullPath, 1);
        ftp.upload(folderName, fileName, inputStream);
    }


    private FileItemResult ftpFileToFileItem(FTPFile ftpFile, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(ftpFile.getName());
        fileItemResult.setSize(ftpFile.getSize());
        fileItemResult.setTime(ftpFile.getTimestamp().getTime());
        fileItemResult.setType(ftpFile.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
        fileItemResult.setPath(folderPath);

        if (fileItemResult.getType() == FileTypeEnum.FILE) {
            fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(folderPath, fileItemResult.getName())));
        }
        return fileItemResult;
    }

}