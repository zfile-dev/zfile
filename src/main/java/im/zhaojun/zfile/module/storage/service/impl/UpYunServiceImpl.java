package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.UpYun;
import com.alibaba.fastjson.JSON;
import com.upyun.Params;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.bo.AuthModel;
import im.zhaojun.zfile.module.storage.model.bo.UploadSignParam;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.UpYunParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpYunServiceImpl extends AbstractBaseFileService<UpYunParam> {

    private static final String DELETE_NO_EMPTY_FOLDERS_MESSAGE = "directory not empty";

    private static final String END_MARK = "g2gCZAAEbmV4dGQAA2VvZg";

    private UpYun upYun;

    @Override
    public void init() {
        upYun = new UpYun(param.getBucketName(), param.getUsername(), param.getPassword());
    }

    @Override
    public List<FileItemResult> fileList(String folderPath) throws Exception {
        ArrayList<FileItemResult> fileItemList = new ArrayList<>();
        String nextMark = null;

        do {
            HashMap<String, String> hashMap = new HashMap<>(24);
            hashMap.put("x-list-iter", nextMark);
            hashMap.put("x-list-limit", "100");
            UpYun.FolderItemIter folderItemIter = upYun.readDirIter(StringUtils.concat(true, param.getBasePath(), folderPath), hashMap);
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
                        fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(folderPath, fileItemResult.getName())));
                    }
                    fileItemList.add(fileItemResult);
                }
            }
        } while (!END_MARK.equals(nextMark));
        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);

        String baseDownloadUrl = StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(fullPath));
        // 判断是否配置了 token 防盗链.
        if (StrUtil.isNotEmpty(param.getToken())) {
            // 如果前面没有补 /, 则自动补 /, 不然生成的防盗链是无效的.
            long etime = System.currentTimeMillis() / 1000 + TimeUnit.MINUTES.toSeconds(param.getTokenTime());
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
        String encodeFullUrl = StringUtils.concat(true, param.getBasePath() + pathAndName);
        Map<String, String> fileInfo;
        try {
            fileInfo = upYun.getFileInfo(encodeFullUrl);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    
        if (fileInfo == null) {
            throw ExceptionUtil.wrapRuntime(new FileNotFoundException());
        }

        String name = FileUtil.getName(pathAndName);
        String folderPath = StringUtils.getParentPath(pathAndName);
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(name);
        fileItemResult.setSize(Long.valueOf(fileInfo.get("size")));
        fileItemResult.setTime(new Date(Long.parseLong(fileInfo.get("date")) * 1000));
        fileItemResult.setPath(folderPath);

        if ("folder".equals(fileInfo.get("type"))) {
            fileItemResult.setType(FileTypeEnum.FOLDER);
        } else {
            fileItemResult.setType(FileTypeEnum.FILE);
            fileItemResult.setUrl(getDownloadUrl(pathAndName));
        }
        return fileItemResult;
    }

    @Override
    public boolean newFolder(String path, String name) {
        String fullPath = StringUtils.concat(true, param.getBasePath(), path, name);
        try {
            return upYun.mkDir(fullPath, true);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(true, param.getBasePath(), path, name);
        try {
            return upYun.deleteFile(fullPath, null);
        } catch (IOException | UpException e) {
            if (e instanceof  UpException) {
                String message = e.getMessage();
                if (StrUtil.contains(message, DELETE_NO_EMPTY_FOLDERS_MESSAGE)) {
                    throw new RuntimeException("非空文件夹不允许删除");
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
        String srcPath = StringUtils.concat(true, param.getBucketName(), param.getBasePath(), path, name);
        String distPath = StringUtils.concat(true, param.getBasePath(), path, newName);

        try {
            return upYun.moveFile(distPath, srcPath);
        } catch (IOException | UpException e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        throw new UnsupportedOperationException("该存储类型不支持此操作");
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        UploadSignParam uploadSignParam = new UploadSignParam();
        uploadSignParam.setPath(StringUtils.concat(param.getBasePath(), path));
        uploadSignParam.setSize(size);
        uploadSignParam.setName(name);
        AuthModel authModel = generatorAuthModel(uploadSignParam);
        return JSON.toJSONString(authModel);
    }

    private static final int UPLOAD_SESSION_EXPIRATION = 1800;

    // 计算签名
    private String sign(String key, String secret, String method, String uri, String policy) {
        String value = method + "&" + uri;
        if (StrUtil.isNotEmpty(policy)) {
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
        String uri = "/" + param.getBucketName();

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

}