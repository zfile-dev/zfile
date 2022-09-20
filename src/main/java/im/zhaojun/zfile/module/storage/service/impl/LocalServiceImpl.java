package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.constant.ZFileConstant;
import im.zhaojun.zfile.core.exception.file.init.InitializeStorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.LocalParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class LocalServiceImpl extends AbstractProxyTransferService<LocalParam> {
    
    private static final String PREVIEW_PARAM_NAME = "preview";

    @Override
    public void init() {
        // 初始化存储源
        File file = new File(param.getFilePath());
        // 校验文件夹是否存在
        if (!file.exists()) {
            String errMsg = StrUtil.format("文件路径:「{}」不存在, 请检查是否填写正确.", file.getAbsolutePath());
            throw new InitializeStorageSourceException(CodeMsg.STORAGE_SOURCE_INIT_FAIL,
                    storageId, errMsg).setResponseExceptionMessage(true);
        }
    }


    @Override
    public List<FileItemResult> fileList(String folderPath) throws FileNotFoundException {
        // 安全检查，以 .. 或 /.. 开头的需拦截, 否则可能会获取到上层文件夹内容.
        if (StrUtil.startWith(folderPath, "..") || StrUtil.startWith(folderPath, "/..")) {
            return Collections.emptyList();
        }

        List<FileItemResult> fileItemList = new ArrayList<>();

        String fullPath = StringUtils.concat(param.getFilePath() + folderPath);

        File file = new File(fullPath);

        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在");
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
        String fullPath = StringUtils.concat(param.getFilePath(), pathAndName);

        File file = new File(fullPath);

        if (!file.exists()) {
            throw ExceptionUtil.wrapRuntime(new FileNotFoundException("文件不存在"));
        }

        String folderPath = StringUtils.getParentPath(pathAndName);
        return fileToFileItem(file, folderPath);
    }


    @Override
    public boolean newFolder(String path, String name) {
        String fullPath = StringUtils.concat(param.getFilePath(), path, name);
        return FileUtil.mkdir(fullPath) != null;
    }


    @Override
    public boolean deleteFile(String path, String name) {
        log.error("删除文件: {}", path + name);
        if (RandomUtil.randomBoolean()) {
            throw new RuntimeException("模拟删除失败");
        }
        String fullPath = StringUtils.concat(param.getFilePath(), path, name);
        return FileUtil.del(fullPath);
    }


    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }


    @Override
    public boolean renameFile(String path, String name, String newName) {
        // 如果文件名没变，不做任何操作.
        if (StrUtil.equals(name, newName)) {
            return true;
        }

        String srcPath = StringUtils.concat(param.getFilePath(), path, name);
        File file = new File(srcPath);
        
        boolean srcExists = file.exists();
        if (!srcExists) {
            throw ExceptionUtil.wrapRuntime(new FileNotFoundException("文件夹不存在."));
        }
        
        FileUtil.rename(file, newName, true);
        return true;
    }


    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }


    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.LOCAL;
    }


    @Override
    public void uploadFile(String pathAndName, InputStream inputStream) {
        String baseFilePath = param.getFilePath();
        String uploadPath = StringUtils.removeDuplicateSlashes(baseFilePath + ZFileConstant.PATH_SEPARATOR + pathAndName);
        // 如果目录不存在则创建
        String parentPath = FileUtil.getParent(uploadPath, 1);
        if (!FileUtil.exist(parentPath)) {
            FileUtil.mkdir(parentPath);
        }

        File uploadToFileObj = new File(uploadPath);
        BufferedOutputStream outputStream = FileUtil.getOutputStream(uploadToFileObj);
        IoUtil.copy(inputStream, outputStream);
    }


    @Override
    public ResponseEntity<Resource> downloadToStream(String pathAndName) {
        File file = new File(StringUtils.removeDuplicateSlashes(param.getFilePath() + ZFileConstant.PATH_SEPARATOR + pathAndName));
        if (!file.exists()) {
            ByteArrayResource byteArrayResource = new ByteArrayResource("文件不存在或异常，请联系管理员.".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(byteArrayResource);
        }

        HttpServletRequest request = RequestHolder.getRequest();
        String type = request.getParameter("type");
    
        MediaType mimeType = MediaType.APPLICATION_OCTET_STREAM;
        if (StrUtil.equals(type, PREVIEW_PARAM_NAME)) {
            mimeType = MediaTypeFactory.getMediaType(file.getName()).orElse(MediaType.APPLICATION_OCTET_STREAM);
        }
    
        HttpHeaders headers = new HttpHeaders();
        
        if (ObjectUtil.equals(mimeType, MediaType.APPLICATION_OCTET_STREAM)) {
            String fileName = file.getName();
            headers.setContentDispositionFormData("attachment", StringUtils.encodeAllIgnoreSlashes(fileName));
        }
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mimeType)
                .body(new FileSystemResource(file));
    }


    private FileItemResult fileToFileItem(File file, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setType(file.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
        fileItemResult.setTime(new Date(file.lastModified()));
        fileItemResult.setSize(file.length());
        fileItemResult.setName(file.getName());
        fileItemResult.setPath(folderPath);

        if (fileItemResult.getType() == FileTypeEnum.FILE) {
            fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(folderPath, file.getName())));
        }
        return fileItemResult;
    }

}