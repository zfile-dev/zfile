package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.crypto.SecureUtil;
import com.UpYun;
import com.alibaba.fastjson2.JSON;
import com.upyun.Params;
import com.upyun.RestManager;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import im.zhaojun.zfile.module.storage.model.bo.AuthModel;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.bo.UploadSignParam;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.UpYunParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpYunServiceImpl extends AbstractProxyTransferService<UpYunParam> {

    private static final String DELETE_NO_EMPTY_FOLDERS_MESSAGE = "directory not empty";

    private static final String END_MARK = "g2gCZAAEbmV4dGQAA2VvZg";

    private UpYun upYun;

    private RestManager restManager;

    private static volatile boolean isFirstUpload = true;

    private static final Lock lock = new ReentrantLock();

    @Override
    public void init() {
        restManager = new RestManager(param.getBucketName(), param.getUsername(), param.getPassword());
        restManager.setTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
        upYun = new UpYun(param.getBucketName(), param.getUsername(), param.getPassword());
        upYun.setTimeout(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_SECONDS);
    }

    @Override
    public List<FileItemResult> fileList(String folderPath) throws Exception {
        ArrayList<FileItemResult> fileItemList = new ArrayList<>();
        String nextMark = null;

        do {
            HashMap<String, String> hashMap = new HashMap<>(24);
            hashMap.put("x-list-iter", nextMark);
            hashMap.put("x-list-limit", "100");
            UpYun.FolderItemIter folderItemIter = upYun.readDirIter(StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), folderPath), hashMap);
            nextMark = folderItemIter.iter;
            ArrayList<UpYun.FolderItem> folderItems = folderItemIter.files;
            if (folderItems != null) {
                for (UpYun.FolderItem folderItem : folderItems) {
                    FileItemResult fileItemResult = new FileItemResult();
                    fileItemResult.setName(folderItem.name);
                    fileItemResult.setSize(folderItem.size);
                    fileItemResult.setTime(folderItem.date);
                    fileItemResult.setPath(folderPath);
                    fileItemResult.setType("folder".equals(folderItem.type) ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
                    if (fileItemResult.getType() == FileTypeEnum.FILE) {
                        String pathAndName = StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName());
                        fileItemResult.setUrl(getDownloadUrl(pathAndName));
                    }
                    fileItemList.add(fileItemResult);
                }
            }
        } while (!END_MARK.equals(nextMark));
        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getDomain())) {
            return super.getProxyDownloadUrl(pathAndName);
        } else {
            return getOriginDownloadUrl(pathAndName);
        }
    }

    public String getOriginDownloadUrl(String pathAndName) {
        String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);

        String domain = StringUtils.isBlank(param.getDomain()) ? "http://" + param.getBucketName() + ".test.upcdn.net" : param.getDomain();

        String baseDownloadUrl = StringUtils.concat(domain, StringUtils.encodeAllIgnoreSlashes(fullPath));
        // 判断是否配置了 token 防盗链.
        if (StringUtils.isNotEmpty(param.getToken())) {
            // 如果前面没有补 /, 则自动补 /, 不然生成的防盗链是无效的.
            long tokenTime = param.getProxyTokenTime();
            long etime = System.currentTimeMillis() / 1000 + TimeUnit.MINUTES.toSeconds(tokenTime);
            String downloadToken = SecureUtil.md5(param.getToken() + "&" + etime + "&" + fullPath).substring(12, 20);
            baseDownloadUrl += "?_upt=" + downloadToken + etime;
        }

        return baseDownloadUrl;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.UPYUN;
    }

    @Override
    public FileItemResult getFileItem(String pathAndName) {
        String encodeFullUrl = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), pathAndName);
        Map<String, String> fileInfo;
        try {
            fileInfo = upYun.getFileInfo(encodeFullUrl);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }

        if (fileInfo == null) {
            return null;
        }

        String name = FileUtils.getName(pathAndName);
        String folderPath = FileUtils.getParentPath(pathAndName);
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(name);
        fileItemResult.setSize(Long.valueOf(fileInfo.get("x-upyun-file-size")));
        fileItemResult.setTime(new Date(Long.parseLong(fileInfo.get("x-upyun-file-date")) * 1000));
        fileItemResult.setPath(folderPath);

        if ("folder".equals(fileInfo.get("x-upyun-file-type"))) {
            fileItemResult.setType(FileTypeEnum.FOLDER);
        } else {
            fileItemResult.setType(FileTypeEnum.FILE);
            fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), pathAndName)));
        }
        return fileItemResult;
    }

    @Override
    public boolean newFolder(String path, String name) {
        String fullPath = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), path, name);
        try {
            return upYun.mkDir(fullPath, true);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), path, name);
        try {
            return upYun.deleteFile(fullPath, null);
        } catch (IOException | UpException e) {
            if (e instanceof  UpException) {
                String message = e.getMessage();
                if (StringUtils.contains(message, DELETE_NO_EMPTY_FOLDERS_MESSAGE)) {
                    throw new BizException(ErrorCode.BIZ_DELETE_FILE_NOT_EMPTY);
                }
            }
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        String srcPath = StringUtils.concat(true, param.getBucketName(), param.getBasePath(), getCurrentUserBasePath(), path, name);
        String distPath = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), path, newName);

        try {
            return upYun.moveFile(distPath, srcPath);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        if (param.isEnableProxyUpload()) {
            return super.getProxyUploadUrl(path, name);
        }
        UploadSignParam uploadSignParam = new UploadSignParam();
        uploadSignParam.setPath(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path));
        uploadSignParam.setSize(size);
        uploadSignParam.setName(name);
        AuthModel authModel = generatorAuthModel(uploadSignParam);
        return JSON.toJSONString(authModel);
    }

    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        String srcPath = StringUtils.concat(true, param.getBucketName(), param.getBasePath(), getCurrentUserBasePath(), path, name);
        String distPath = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), targetPath, targetName);

        try {
            return upYun.copyFile(distPath, srcPath);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        String srcPath = StringUtils.concat(true, param.getBucketName(), param.getBasePath(), getCurrentUserBasePath(), path, name);
        String distPath = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), targetPath, targetName);

        try {
            return upYun.moveFile(distPath, srcPath);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
    }

    private static final int UPLOAD_SESSION_EXPIRATION = 1800;

    // 计算签名
    private String sign(String key, String secret, String method, String uri, String policy) {
        String value = method + "&" + uri;
        if (StringUtils.isNotEmpty(policy)) {
            value = value + "&" + policy;
        }
        byte[] hmac = SecureUtil.hmacSha1(secret).digest(value);
        String sign = Base64.getEncoder().encodeToString(hmac);
        return "UPYUN " + key + ":" + sign;
    }

    // 计算上传签名
    public AuthModel generatorAuthModel(UploadSignParam uploadSignParam) {
        String policy = getPolicy(uploadSignParam);

        String method = "POST";
        String uri = StringUtils.SLASH + param.getBucketName();

        // 上传，处理，内容识别有存储
        String signature = sign(param.getUsername(), SecureUtil.md5(param.getPassword()), method, uri, policy);

        return new AuthModel("https://v0.api.upyun.com/" + param.getBucketName(), signature, policy);
    }

    /**
     * 获取上传 policy
     *
     * @param   uploadSignParam
     *          上传签名参数
     *
     * @return  上传 policy
     */
    private String getPolicy(UploadSignParam uploadSignParam) {
        String bucketName = param.getBucketName();
        HashMap<String, Object> params = new HashMap<>();
        params.put(Params.BUCKET, bucketName);
        params.put(Params.SAVE_KEY, StringUtils.concat(uploadSignParam.getPath(), uploadSignParam.getName()));
        params.put(Params.EXPIRATION, System.currentTimeMillis() / 1000 + UPLOAD_SESSION_EXPIRATION);
        params.put("content-length", uploadSignParam.getSize());
        params.put(Params.CONTENT_LENGTH_RANGE, "0," + uploadSignParam.getSize());
        return UpYunUtils.getPolicy(params);
    }

    /**
     * 第一次上传时需加锁，不然又拍云这个上传 API 可能会遇到并发异常
     */
    @Override
    public void uploadFile(String pathAndName, InputStream inputStream, Long size) throws IOException, UpException {
        boolean doLock = isFirstUpload;

        if (doLock) {
            lock.lock(); // 在第一次上传时加锁
            try {
                // 再次检查以确保 isFirstUpload 没有变更
                if (isFirstUpload) {
                    tryUpload(pathAndName, inputStream);
                    isFirstUpload = false; // 第一次上传后修改标志
                }
            } finally {
                lock.unlock(); // 释放锁
            }
        } else {
            // 对于后续的上传，直接处理，无需锁
            tryUpload(pathAndName, inputStream);
        }
    }

    private void tryUpload(String pathAndName, InputStream inputStream) throws IOException, UpException {
        String encodeFullUrl = StringUtils.concat(true, param.getBasePath(), getCurrentUserBasePath(), pathAndName);
        boolean isSuccess = upYun.writeFile(encodeFullUrl, inputStream, true, null);
        if (!isSuccess) {
            log.error("又拍云上传失败，pathAndName：{}", pathAndName);
            throw new UpException("上传失败"); // 抛出异常，便于上层处理错误
        }
    }

    @Override
    public ResponseEntity<Resource> downloadToStream(String pathAndName) throws Exception {
        String fullUrl = StringUtils.concat(param.getBasePath(), pathAndName);
        Response response = restManager.readFile(fullUrl);
        InputStream inputStream = response.body().byteStream();
        String fileName = FileUtils.getName(pathAndName);
        long fileSize = Convert.toLong(response.header(HttpHeaders.CONTENT_LENGTH, "0"));
        RequestHolder.writeFile(inputStream, fileName, fileSize, false, param.isProxyLinkForceDownload());
        return null;
    }

    @Override
    public StorageSourceMetadata getStorageSourceMetadata() {
        StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
        if (param.isEnableProxyUpload()) {
            storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
        } else {
            storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.UPYUN);
        }
        storageSourceMetadata.setSupportRenameFolder(false);
        storageSourceMetadata.setSupportMoveFolder(false);
        storageSourceMetadata.setSupportCopyFolder(false);
        storageSourceMetadata.setSupportDeleteNotEmptyFolder(false);
        storageSourceMetadata.setNeedCreateFolderBeforeUpload(false);
        return storageSourceMetadata;
    }
}