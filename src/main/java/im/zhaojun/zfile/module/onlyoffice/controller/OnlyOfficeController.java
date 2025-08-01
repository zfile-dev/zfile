package im.zhaojun.zfile.module.onlyoffice.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.fastjson2.JSONObject;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.module.onlyoffice.model.OnlyOfficeCallback;
import im.zhaojun.zfile.core.exception.biz.InvalidStorageSourceBizException;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.*;
import im.zhaojun.zfile.module.onlyoffice.model.OnlyOfficeFile;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.annotation.CheckPassword;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.model.request.base.FileItemRequest;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.Beans;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Tag(name = "OnlyOffice 相关接口")
@RestController
@RequestMapping("/onlyOffice")
public class OnlyOfficeController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private UserStorageSourceService userStorageSourceService;

    private static final String CALLBACK_ERROR_MSG = "{\"error\":1}";

    private static final String CALLBACK_SUCCESS_MSG = "{\"error\":0}";

    public static final List<Integer> SUPPORTED_STATUS = List.of(2, 3, 6, 7);

    @ApiOperationSupport(order = 3)
    @Operation(summary = "OnlyOffice 预览文件", description = "根据传入的文件信息, 生成 OnlyOffice 预览所需的 JSON 数据.")
    @PostMapping("/config/token")
    @CheckPassword(storageKeyFieldExpression = "[0].storageKey",
            pathFieldExpression = "[0].path",
            pathIsDirectory = false,
            passwordFieldExpression = "[0].password")
    public AjaxJson<JSONObject> getPreviewFileJSONInfo(@Valid @RequestBody FileItemRequest fileItemRequest) {
        // 根据存储策略获取文件信息(下载地址), 会校验权限.
        Pair<FileItemResult, Boolean> pair = getFileInfo(fileItemRequest);
        FileItemResult fileInfo = pair.getKey();
        Boolean hasUploadPermission = pair.getRight();

        // 为 OnlyOffice 获取或生成文件 Key.
        OnlyOfficeFile onlyOfficeFile = new OnlyOfficeFile(fileItemRequest.getStorageKey(), fileItemRequest.getPath());
        String key = OnlyOfficeKeyCacheUtils.getKeyOrPutNew(onlyOfficeFile, 3000);

        JSONObject onlyOfficePayload = createOnlyOfficePayload(fileInfo, key, hasUploadPermission);
        return AjaxJson.getSuccessData(onlyOfficePayload);
    }

    private Pair<FileItemResult, Boolean> getFileInfo(FileItemRequest fileItemRequest) {
        String storageKey = fileItemRequest.getStorageKey();
        Integer storageId = storageSourceService.findIdByKey(storageKey);
        if (storageId == null) {
            throw new InvalidStorageSourceBizException(storageKey);
        }

        // 处理请求参数默认值
        fileItemRequest.handleDefaultValue();

        // 获取文件信息
        AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageId(storageId);
        try {
            FileItemResult fileItem = fileService.getFileItem(fileItemRequest.getPath());
            if (fileItem == null) {
                throw new BizException("文件不存在");
            }

            String currentUserBasePath = fileService.getCurrentUserBasePath();
            fileItemRequest.setPath(StringUtils.concat(currentUserBasePath, fileItemRequest.getPath()));

            boolean hasUploadPermission = userStorageSourceService.hasCurrentUserStorageOperatorPermission(storageId, FileOperatorTypeEnum.UPLOAD);
            return Pair.of(fileItem, hasUploadPermission);
        } catch (Exception e) {
            throw new BizException("获取文件信息失败: " + e.getMessage());
        }
    }


    /**
     * 生成 OnlyOffice 预览所需的 JSON 数据. 配置参考: <a href="https://api.onlyoffice.com/zh/editors/config/editor" />
     *
     * @param   fileItemResult
     *          文件信息
     *
     * @param   key
     *          OnlyOffice JWT 密钥
     *
     * @param   hasUploadPermission
     *          是否有上传(编辑)权限
     *
     * @return  OnlyOffice 预览所需的 JSON 数据, 包含 JWT 密钥
     */
    private JSONObject createOnlyOfficePayload(FileItemResult fileItemResult, String key, boolean hasUploadPermission) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("width", "100%");
        jsonObject.put("height", "100%");

        jsonObject.put("document", new JSONObject()
                .fluentPut("fileType", FileUtils.getExtension(fileItemResult.getName()))
                .fluentPut("key", key)
                .fluentPut("permissions", new JSONObject()
                        .fluentPut("edit", hasUploadPermission))
                .fluentPut("title", fileItemResult.getName())
                .fluentPut("url", fileItemResult.getUrl())
                .fluentPut("lang", "zh-CN"));

        User currentUser = ZFileAuthUtil.getCurrentUser();

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String onlyOfficeSecret = systemConfig.getOnlyOfficeSecret();


        jsonObject.put("editorConfig", new JSONObject()
                .fluentPut("callbackUrl", StringUtils.concat(systemConfigService.getAxiosFromDomainOrSetting(), "/onlyOffice/callback"))
                .fluentPut("lang", "zh-CN")
                .fluentPut("user", new JSONObject()
                        .fluentPut("id", currentUser.getId())
                        .fluentPut("name", StringUtils.firstNonNull(currentUser.getNickname(), currentUser.getUsername()))));

        if (StringUtils.isNotEmpty(onlyOfficeSecret)) {
            String token = JWTUtil.createToken(jsonObject, onlyOfficeSecret.getBytes(StandardCharsets.UTF_8));
            jsonObject.put("token", token);
        }

        return jsonObject;
    }

    @RequestMapping("/callback")
    public String callBack(@RequestBody OnlyOfficeCallback onlyOfficeCallback) {
        log.debug("OnlyOffice 回调信息: {}, {}", onlyOfficeCallback.getStatus(), onlyOfficeCallback);
        boolean useOnlyOfficeSecret = StrUtil.isNotBlank(systemConfigService.getSystemConfig().getOnlyOfficeSecret());
        if (useOnlyOfficeSecret) {

            if (StrUtil.isBlank(onlyOfficeCallback.getToken())) {
                log.error("OnlyOffice 回调 Token 为空: {}", onlyOfficeCallback);
                return CALLBACK_ERROR_MSG;
            }

            if (!JWTUtil.verify(onlyOfficeCallback.getToken(), StrUtil.bytes(systemConfigService.getSystemConfig().getOnlyOfficeSecret(), StandardCharsets.UTF_8))) {
                log.error("OnlyOffice 回调 Token 验证失败: {}", onlyOfficeCallback);
                return CALLBACK_ERROR_MSG;
            }

        }
        // 文件发送了变化，清空缓存中该文件的 key 信息.
        if (SUPPORTED_STATUS.contains(onlyOfficeCallback.getStatus())) {
            String key = onlyOfficeCallback.getKey();
            OnlyOfficeFile onlyOfficeFile = OnlyOfficeKeyCacheUtils.removeByKey(key);
            ReentrantLock lock = OnlyOfficeKeyCacheUtils.getLock(onlyOfficeFile);
            lock.lock();
            log.debug("开始处理 OnlyOffice 文件: {}, 加锁", key);
            try {
                // 文件不存在或者存储策略不存在, 直接返回错误信息.
                if (onlyOfficeFile == null) {
                    return CALLBACK_ERROR_MSG;
                }
                AbstractBaseFileService<?> storageServiceByKey = StorageSourceContext.getByStorageKey(onlyOfficeFile.getStorageKey());
                if (storageServiceByKey == null) {
                    return CALLBACK_ERROR_MSG;
                }

                String userId = CollUtil.getFirst(onlyOfficeCallback.getUsers());
                if (StringUtils.isNotBlank(userId)) {
                    StpUtil.login(userId);
                }

                log.debug("开始保存 OnlyOffice 文件: {}, {}", onlyOfficeFile.getStorageKey(), onlyOfficeFile.getPathAndName());

                if (Beans.isInstanceOf(storageServiceByKey, AbstractProxyTransferService.class)) {
                    // 进行上传.
                    AbstractProxyTransferService<?> proxyUploadService = (AbstractProxyTransferService<?>) storageServiceByKey;

                    try {
                        URL url = new URI(onlyOfficeCallback.getUrl()).toURL();
                        URLConnection connection = url.openConnection();
                        long contentLength = connection.getContentLengthLong();
                        try (InputStream inputStream = connection.getInputStream()) {
                            String pathAndName = onlyOfficeFile.getPathAndName();
                            proxyUploadService.uploadFile(pathAndName, inputStream, contentLength);
                        }
                    } catch (Exception e) {
                        log.error("回调保存 OnlyOffice 文件失败", e);
                        return CALLBACK_ERROR_MSG;
                    }
                }

                log.debug("完成保存 OnlyOffice 文件: {}, {}", onlyOfficeFile.getStorageKey(), onlyOfficeFile.getPathAndName());
            } finally {
                log.debug("完成处理 OnlyOffice 文件: {}, 解锁", key);
                lock.unlock();
            }
        }
        return CALLBACK_SUCCESS_MSG;
    }
}
