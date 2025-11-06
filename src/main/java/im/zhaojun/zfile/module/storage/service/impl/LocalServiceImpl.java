package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.FilePathSecurityBizException;
import im.zhaojun.zfile.core.exception.biz.InitializeStorageSourceBizException;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.LocalParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class LocalServiceImpl extends AbstractProxyTransferService<LocalParam> {

    @Override
    public void init() {
        // 初始化存储源
        File file = new File(param.getFilePath());
        // 校验文件夹是否存在
        if (!file.exists()) {
            String errMsg = String.format("文件路径:「%s」不存在, 请检查是否填写正确.", file.getAbsolutePath());
            throw new InitializeStorageSourceBizException(errMsg, storageId);
        }
    }


    @Override
    public List<FileItemResult> fileList(String folderPath) throws FileNotFoundException {
        checkPathSecurity(folderPath);

        List<FileItemResult> fileItemList = new ArrayList<>();

        String fullPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), folderPath);

        File file = new File(fullPath);

        if (!(file.isDirectory() && file.exists())) {
            throw new BizException(ErrorCode.BIZ_FOLDER_NOT_EXIST);
        }

        File[] files = file.listFiles();

        if (files == null) {
            return fileItemList;
        }
        for (File f : files) {
            fileItemList.add(fileToFileItem(f, folderPath));
        }

        return fileItemList;
    }


    @Override
    public FileItemResult getFileItem(String pathAndName) {
        checkPathSecurity(pathAndName);

        String fullPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), pathAndName);

        File file = new File(fullPath);

        if (!file.exists()) {
            return null;
        }

        String folderPath = FileUtils.getParentPath(pathAndName);
        return fileToFileItem(file, folderPath);
    }


    @Override
    public boolean newFolder(String path, String name) {
        checkPathSecurity(path);
        checkNameSecurity(name);

        String fullPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), path, name);
        return FileUtil.mkdir(fullPath) != null;
    }


    @Override
    public boolean deleteFile(String path, String name) {
        checkPathSecurity(path);
        checkNameSecurity(name);

        String fullPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), path, name);
        return FileUtil.del(fullPath);
    }


    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }


    @Override
    public boolean renameFile(String path, String name, String newName) {
        return operateFile(path, name, path, newName, FileOperatorTypeEnum.RENAME);
    }


    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return operateFile(path, name, path, newName, FileOperatorTypeEnum.RENAME);
    }


    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.LOCAL;
    }


    @Override
    public void uploadFile(String pathAndName, InputStream inputStream, Long size) {
        checkPathSecurity(pathAndName);

        String uploadPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), pathAndName);

        // 如果目录不存在则创建
        String parentPath = FileUtils.getParentPath(uploadPath);
        if (!FileUtil.exist(parentPath)) {
            FileUtil.mkdir(parentPath);
        }

        File uploadToFileObj = new File(uploadPath);
        BufferedOutputStream outputStream = FileUtil.getOutputStream(uploadToFileObj);
        IoUtil.copy(inputStream, outputStream);
        IoUtil.close(outputStream);
        IoUtil.close(inputStream);
    }

    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        return operateFile(path, name, targetPath, targetName, FileOperatorTypeEnum.COPY);
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        return operateFile(path, name, targetPath, targetName, FileOperatorTypeEnum.COPY);
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        return operateFile(path, name, targetPath, targetName, FileOperatorTypeEnum.MOVE);
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        return operateFile(path, name, targetPath, targetName, FileOperatorTypeEnum.MOVE);
    }

    private boolean operateFile(String path, String name, String newPath, String newName, FileOperatorTypeEnum operatorTypeEnum) {
        checkPathSecurity(path, newPath);
        checkNameSecurity(name, newName);

        // 如果原文件路径和目的文件路径没变，不做任何操作.
        if (StringUtils.equals(path, newPath) && StringUtils.equals(name, newName)) {
            return true;
        }

        String srcPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), path, name);

        // 如果是复制，不需要拼接新的文件名
        String appendName = operatorTypeEnum == FileOperatorTypeEnum.COPY ? "" : newName;
        String distPath = StringUtils.concat(param.getFilePath(), getCurrentUserBasePath(), newPath, appendName);
        File srcFile = new File(srcPath);
        File distFile = new File(distPath);

        if (operatorTypeEnum == FileOperatorTypeEnum.MOVE) {
            FileUtil.move(srcFile, distFile, true);
        } else if (operatorTypeEnum == FileOperatorTypeEnum.COPY) {
            FileUtil.copy(srcFile, distFile, true);
        } else if (operatorTypeEnum == FileOperatorTypeEnum.RENAME) {
            FileUtil.rename(srcFile, newName, true);
        } else {
            throw ExceptionUtil.wrapRuntime(new RuntimeException("不支持的操作类型."));
        }
        return true;
    }


    @Override
    public String getUploadUrl(String path, String name, Long size) {
        return super.getProxyUploadUrl(path, name);
    }


    @Override
    public String getDownloadUrl(String pathAndName) {
        if (StringUtils.isNotBlank(param.getDomain())) {
            return StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(pathAndName));
        }
        return super.getProxyDownloadUrl(pathAndName);
    }


    @Override
    public ResponseEntity<Resource> downloadToStream(String pathAndName) {
        checkPathSecurity(pathAndName);

        File file = new File(StringUtils.concat(param.getFilePath(), pathAndName));
        if (!file.exists()) {
            throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }

        Resource body = new FileSystemResource(file);

        MediaType mimeType;
        if (param.isProxyLinkForceDownload()) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            mimeType = MediaTypeFactory.getMediaType(file.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM);
        }

        HttpHeaders headers = new HttpHeaders();
        String fileName = file.getName();

        ContentDisposition contentDisposition = ContentDisposition
                .builder(ObjectUtil.equals(mimeType, MediaType.APPLICATION_OCTET_STREAM) ? "attachment" : "inline")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mimeType)
                .body(body);
    }


    private FileItemResult fileToFileItem(File file, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setType(file.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
        fileItemResult.setTime(new Date(file.lastModified()));
        fileItemResult.setSize(file.length());
        fileItemResult.setName(file.getName());
        fileItemResult.setPath(folderPath);

        if (fileItemResult.getType() == FileTypeEnum.FILE) {
            fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), folderPath, file.getName())));
        } else {
            fileItemResult.setSize(null);
        }
        return fileItemResult;
    }


    /**
     * 检查路径合法性：
     *  - 只有以 . 开头的允许通过，其他的如 ./ ../ 的都是非法获取上层文件夹内容的路径.
     *
     * @param   paths
     *          文件路径
     *
     * @throws IllegalArgumentException    文件路径包含非法字符时会抛出此异常
     */
    private static void checkPathSecurity(String... paths) {
        for (String path : paths) {
            // 路径中不能包含 .. 不然可能会获取到上层文件夹的内容
            if (StringUtils.startWith(path, "/..") || StringUtils.containsAny(path, "../", "..\\")) {
                throw new FilePathSecurityBizException(path);
            }
        }
    }


    /**
     * 检查路径合法性：
     *  - 不为空，且不包含 \ / 字符
     *
     * @param   names
     *          文件路径
     *
     * @throws IllegalArgumentException    文件名包含非法字符时会抛出此异常
     */
    private static void checkNameSecurity(String... names) {
        for (String name : names) {
            // 路径中不能包含 .. 不然可能会获取到上层文件夹的内容
            if (StringUtils.containsAny(name, "\\", StringUtils.SLASH)) {
                throw new FilePathSecurityBizException(name);
            }
        }
    }

    @Override
    public StorageSourceMetadata getStorageSourceMetadata() {
        StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
        storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
        storageSourceMetadata.setNeedCreateFolderBeforeUpload(false);
        return storageSourceMetadata;
    }

}