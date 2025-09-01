package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import im.zhaojun.zfile.module.storage.controller.helper.Open115UploadUtils;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import im.zhaojun.zfile.core.util.FileSizeConverter;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageConfigConstant;
import im.zhaojun.zfile.module.storage.controller.proxy.Open115UrlController;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.dto.RefreshTokenInfoDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.Open115Param;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.StorageSourceConfigService;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import im.zhaojun.zfile.module.storage.support.Open115IdCacheService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class Open115ServiceImpl extends AbstractProxyTransferService<Open115Param>  implements RefreshTokenService {

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    /**
     * 默认 User-Agent, 用于获取下载地址时使用.
     */
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36";

    /**
     * 访问令牌字段名称
     */
    public static final String ACCESS_TOKEN_FIELD_NAME = "access_token";

    /**
     * 刷新令牌字段名称
     */
    public static final String REFRESH_TOKEN_FIELD_NAME = "refresh_token";

    /**
     * 过期时间字段名称
     */
    public static final String EXPIRES_IN_FIELD_NAME = "expires_in";

    /**
     * 分页最大每页条数限制
     */
    public static final Integer FILE_LIST_LIMIT = 1150;

    /**
     * 文件类型: 文件
     */
    private static final String FC_FILE = "1";

    /**
     * 下载地址缓存: 默认过期时间 50 分钟 (115 下载地址到期时间为 1 小时)， key 为 pick_code + userAgent
     */
    private final Cache<String, String> DOWNLOAD_URL_CACHE = CacheUtil.newTimedCache(50 * 60 * 1000,10 * 60 * 1000);

    /**
     * 访问频率控制器, 用于限制 QPS, 避免请求过快被 115 限制.
     */
    private RateLimiter rateLimiter;

    /**
     * ID 缓存服务
     */
    private Open115IdCacheService idCacheService;

    @Override
    public void init() {
        this.rateLimiter = RateLimiter.create(param.getQps());
        this.idCacheService = new Open115IdCacheService(this::sendGetRequestWithAuth);

        Integer refreshTokenExpiredAt = param.getRefreshTokenExpiredAt();
        if (refreshTokenExpiredAt == null) {
            refreshAccessToken();
        } else {
            RefreshTokenInfoDTO tokenInfoDTO = RefreshTokenInfoDTO.success(param.getAccessToken(), param.getRefreshToken(), refreshTokenExpiredAt);
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));
        }
    }

    @Override
    public List<FileItemResult> fileList(String folderPath) throws Exception {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath);
        String pathId = idCacheService.getPathId(fullPath, true);
        List<FileItemResult> result = new ArrayList<>();

        int offset = 0;
        int count;
        do {
            // https://www.yuque.com/115yun/open/kz9ft9a7s57ep868
            JSONObject jsonObject = sendGetRequestWithAuth("https://proapi.115.com/open/ufile/files", new JSONObject()
                    .fluentPut("cid", pathId)
                    .fluentPut("offset", offset)
                    .fluentPut("limit", FILE_LIST_LIMIT)
                    .fluentPut("show_dir", 1));

            // 如果请求 id 与返回 id 不符，是 115 做了兼容处理，直接返回了根目录
            String cid = jsonObject.getString("cid");
            if (StringUtils.isNotBlank(pathId) && !Objects.equals(cid, pathId)) {
                log.warn("请求的路径 ID '{}' 与返回的路径 ID '{}' 不符, 可能是 115 做了兼容处理, 返回了根目录.", pathId, cid);
                throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
            }

            JSONArray fileList = jsonObject.getJSONArray("data");
            for (int i = 0; i < fileList.size(); i++) {
                JSONObject fileItem = fileList.getJSONObject(i);
                FileItemResult fileItemResult = listJsonToFileItem(fileItem, folderPath);
                result.add(fileItemResult);
            }

            count = jsonObject.getInteger("count");
            offset += FILE_LIST_LIMIT;
        } while (result.size() < count);

        return result;
    }

    @Override
    public FileItemResult getFileItem(String pathAndName) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), pathAndName);
        String fileId = idCacheService.getFileId(fullPath, false);
        if (fileId == null) {
            fileId = idCacheService.getPathId(fullPath, true);
        }

        // https://www.yuque.com/115yun/open/rl8zrhe2nag21dfw
        JSONObject jsonObject = sendGetRequestWithAuth("https://proapi.115.com/open/folder/get_info", new JSONObject()
                .fluentPut("file_id", fileId));

        JSONObject fileItem = jsonObject.getJSONObject("data");
        return itemJsonToFileItem(fileItem, FileUtils.getParentPath(pathAndName));
    }

    @Override
    public boolean newFolder(String path, String name) {
        if (StringUtils.length(name) > 255) {
            throw new BizException("文件夹名称过长, 不能超过 255 个字符.");
        }
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path);
        String pathId = idCacheService.getPathId(fullPath, true);

        // https://www.yuque.com/115yun/open/qur839kyx9cgxpxi
        JSONObject jsonObject = sendPostRequestWithAuth("https://proapi.115.com/open/folder/add", new JSONObject()
                .fluentPut("pid", pathId)
                .fluentPut("file_name", name));

        JSONObject data = jsonObject.getJSONObject("data");
        String fileId = data.getString("file_id");
        idCacheService.putPathId(StringUtils.concat(fullPath, name), fileId);
        return true;
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String deleteFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String deleteParentPath = FileUtils.getParentPath(deleteFullPath);

        String fileId = idCacheService.getFileId(deleteFullPath, true);
        String parentPathId = idCacheService.getPathId(deleteParentPath, true);

        // https://www.yuque.com/115yun/open/kt04fu8vcchd2fnb
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/delete", new JSONObject()
                .fluentPut("parent_id", parentPathId)
                .fluentPut("file_ids", fileId));

        idCacheService.deleteFileId(deleteFullPath);
        return true;
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        String deleteFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String deleteParentPath = FileUtils.getParentPath(deleteFullPath);

        String pathId = idCacheService.getPathId(deleteFullPath, true);
        String parentPathId = idCacheService.getPathId(deleteParentPath, true);

        // https://www.yuque.com/115yun/open/kt04fu8vcchd2fnb
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/delete", new JSONObject()
                .fluentPut("parent_id", parentPathId)
                .fluentPut("file_ids", pathId));

        idCacheService.deletePathId(deleteFullPath);
        return true;
    }

    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        String srcFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String targetFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath);

        String srcFileId = idCacheService.getFileId(srcFullPath, true);
        String targetPathId = idCacheService.getPathId(targetFullPath, true);

        // https://www.yuque.com/115yun/open/lvas49ar94n47bbk
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/copy", new JSONObject()
                .fluentPut("pid", targetPathId)
                .fluentPut("file_id", srcFileId)
                .fluentPut("nodupli", 1));
        return true;
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        String srcFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String targetFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath);

        String srcPathId = idCacheService.getPathId(srcFullPath, true);
        String targetPathId = idCacheService.getPathId(targetFullPath, true);

        // https://www.yuque.com/115yun/open/lvas49ar94n47bbk
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/copy", new JSONObject()
                .fluentPut("pid", targetPathId)
                .fluentPut("file_id", srcPathId)
                .fluentPut("nodupli", 1));

        return true;
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        String srcFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String targetFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath, targetName);

        String srcFileId = idCacheService.getFileId(srcFullPath, true);
        String targetPathId = idCacheService.getPathId(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath), true);

        // https://www.yuque.com/115yun/open/vc6fhi2mrkenmav2
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/move", new JSONObject()
                .fluentPut("to_cid", targetPathId)
                .fluentPut("file_ids", srcFileId));

        String id = idCacheService.removeFileIdByPath(srcFullPath);
        idCacheService.putFileId(targetFullPath, id);
        return true;
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        String srcFullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);

        String srcPathId = idCacheService.getPathId(srcFullPath, true);
        String targetPathId = idCacheService.getPathId(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath), true);

        // https://www.yuque.com/115yun/open/vc6fhi2mrkenmav2
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/move", new JSONObject()
                .fluentPut("to_cid", targetPathId)
                .fluentPut("file_ids", srcPathId));

        idCacheService.deletePathId(srcFullPath);
        return true;
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        if (StringUtils.length(newName) > 255) {
            throw new BizException("文件夹名称过长, 不能超过 255 个字符.");
        }
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String fileId = idCacheService.getFileId(fullPath, true);

        // https://www.yuque.com/115yun/open/gyrpw5a0zc4sengm
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/update", new JSONObject()
                .fluentPut("file_id", fileId)
                .fluentPut("file_name", newName));

        idCacheService.deletePathId(fullPath);
        idCacheService.putFileId(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, newName), fileId);
        return true;
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        if (StringUtils.length(newName) > 255) {
            throw new BizException("文件夹名称过长, 不能超过 255 个字符.");
        }
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String pathId = idCacheService.getPathId(fullPath, true);

        // https://www.yuque.com/115yun/open/gyrpw5a0zc4sengm
        sendPostRequestWithAuth("https://proapi.115.com/open/ufile/update", new JSONObject()
                .fluentPut("file_id", pathId)
                .fluentPut("file_name", newName));

        idCacheService.deletePathId(fullPath);
        idCacheService.putPathId(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, newName), pathId);
        return true;
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getDomain())) {
            return getProxyDownloadUrl(pathAndName);
        } else {
            FileItemResult fileItem = getFileItem(pathAndName);
            if (fileItem == null || fileItem.getType() != FileTypeEnum.FILE) {
                throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
            }
            return fileItem.getUrl();
        }
    }

    private String getProxyDownloadUrlByPickCode(String pathAndName, String pickCode) {
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getDomain())) {
            return getProxyDownloadUrl(pathAndName);
        } else {
            return StringUtils.concat(getSystemConfigService().getAxiosFromDomainOrSetting(), Open115UrlController.PROXY_DOWNLOAD_LINK_PREFIX, storageId + "", pickCode);
        }
    }

    public String getOpen115DownloadUrlByPickCode(String pickCode) {
        HttpServletRequest request = RequestHolder.getRequest();
        String userAgent;
        if (StringUtils.isBlank(request.getHeader(HttpHeaders.USER_AGENT))) {
            userAgent = DEFAULT_USER_AGENT;
        } else {
            userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        }

        return DOWNLOAD_URL_CACHE.get(pickCode + "_" + userAgent, () -> {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put(HttpHeaders.USER_AGENT, Collections.singletonList(userAgent));

            // https://www.yuque.com/115yun/open/um8whr91bxb5997o
            JSONObject jsonObject = sendRequest("https://proapi.115.com/open/ufile/downurl", Method.POST, true,
                    new JSONObject().fluentPut("pick_code", pickCode),
                    headers
            );

            String finalUrl = null;
            JSONObject dataObject = jsonObject.getJSONObject("data");
            if (dataObject != null && !dataObject.isEmpty()) {
                Set<String> keys = dataObject.keySet();
                String dynamicKey = keys.iterator().next();
                JSONObject fileObject = dataObject.getJSONObject(dynamicKey);
                if (fileObject != null) {
                    JSONObject urlObject = fileObject.getJSONObject("url");
                    if (urlObject != null) {
                        finalUrl = urlObject.getString("url");
                    }
                }
            }

            if (finalUrl == null) {
                throw new BizException(ErrorCode.BIZ_FILE_NOT_EXIST);
            }

            return finalUrl;
        });
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        return super.getProxyUploadUrl(path, name);
    }

    @Override
    public void uploadFile(String pathAndName, InputStream inputStream, Long size) throws Exception {
        File tempFile = File.createTempFile("open115-upload-", ".tmp");
        try {
            // 将 inputStream 写入到 tempFile
            IOUtils.copy(inputStream, new FileOutputStream(tempFile));

            String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), pathAndName);
            String folderPath = FileUtils.getParentPath(fullPath);
            String fileName = FileUtils.getName(fullPath);
            String pathId = idCacheService.getPathId(folderPath, true);

            Open115UploadUtils.uploadFile(tempFile, fileName, pathId, this::checkExpiredAndGetAccessToken);
        } finally {
            boolean delete = tempFile.delete();
            if (!delete) {
                log.warn("上传 115 时无法删除临时文件: {}", tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadToStream(String pathAndName) throws Exception {
        String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);
        String fileId = idCacheService.getFileId(fullPath, true);

        // https://www.yuque.com/115yun/open/rl8zrhe2nag21dfw
        JSONObject fileInfoJSONObj = sendGetRequestWithAuth("https://proapi.115.com/open/folder/get_info", new JSONObject().fluentPut("file_id", fileId));
        String pickCode = fileInfoJSONObj.getJSONObject("data").getString("pick_code");
        String originUrl = getOpen115DownloadUrlByPickCode(pickCode);

        HttpServletRequest request = RequestHolder.getRequest();
        HttpRequest httpRequest = HttpUtil.createGet(originUrl);
        httpRequest.header(HttpHeaders.RANGE, request.getHeader(HttpHeaders.RANGE));
        httpRequest.header(HttpHeaders.USER_AGENT, RequestHolder.getRequest().getHeader(HttpHeaders.USER_AGENT), true);
        HttpResponse httpResponse = httpRequest.executeAsync();

        try {
            HttpServletResponse response = RequestHolder.getResponse();
            response.setStatus(httpResponse.getStatus());
            OutputStream outputStream = response.getOutputStream();

            Map<String, List<String>> headers = httpResponse.headers();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                response.setHeader(key, String.join(",", value));
            }
            httpResponse.writeBody(outputStream, true, null);
        } catch (Exception e) {
            if (StringUtils.contains(e.getMessage(), "ClientAbortException")) {
                // ignore 客户端中止异常
            } else {
                throw e;
            }
        }

        return null;
    }

    @Override
    public void refreshAccessToken() {
        try {
            RefreshTokenInfoDTO tokenInfoDTO = getAndRefreshToken();

            StorageSourceConfig accessTokenConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
            StorageSourceConfig refreshTokenConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);
            StorageSourceConfig refreshTokenExpiredAtConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_EXPIRED_AT_KEY);
            accessTokenConfig.setValue(tokenInfoDTO.getAccessToken());
            refreshTokenConfig.setValue(tokenInfoDTO.getRefreshToken());
            refreshTokenExpiredAtConfig.setValue(String.valueOf(tokenInfoDTO.getExpiredAt()));

            storageSourceConfigService.updateBatch(storageId, Arrays.asList(accessTokenConfig, refreshTokenConfig, refreshTokenExpiredAtConfig));
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));

        } catch (Exception e) {
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.fail(getStorageTypeEnum().getDescription() + " AccessToken 刷新失败: " + e.getMessage()));
            throw new SystemException("存储源 " + storageId + " 刷新令牌失败, 获取时发生异常.", e);

        }
    }

    @Override
    public StorageSourceMetadata getStorageSourceMetadata() {
        StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
        storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
        return storageSourceMetadata;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.OPEN115;
    }

    /**
     * 根据 RefreshToken 刷新 AccessToken, 返回刷新后的 Token.
     *
     * @return  刷新后的 Token
     */
    private RefreshTokenInfoDTO getAndRefreshToken() {
        StorageSourceConfig refreshStorageSourceConfig =
                storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);

        String value = refreshStorageSourceConfig.getValue();

        // https://www.yuque.com/115yun/open/opnx8yezo4at2be6
        JSONObject jsonObject = sendRequest("https://passportapi.115.com/open/refreshToken", Method.POST, false,
                Map.of("refresh_token", refreshStorageSourceConfig.getValue()),
                null);

        JSONObject jsonBody = jsonObject.getJSONObject("data");
        String accessToken = jsonBody.getString(ACCESS_TOKEN_FIELD_NAME);
        String refreshToken = jsonBody.getString(REFRESH_TOKEN_FIELD_NAME);
        Integer expiresIn = jsonBody.getInteger(EXPIRES_IN_FIELD_NAME);
        return RefreshTokenInfoDTO.success(accessToken, refreshToken, expiresIn);
    }

    /**
     * 检查 AccessToken 是否过期，如果过期则刷新 AccessToken 并返回新的 AccessToken。
     */
    private String checkExpiredAndGetAccessToken() {
        RefreshTokenCacheBO.RefreshTokenInfo refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);

        if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
            // 使用双重检查锁定机制，确保同一个 storageId 只会有一个线程在刷新 AccessToken
            synchronized (("storage-refresh-" + storageId).intern()) {
                // 双重检查，再次从缓存中获取，确认是否其他线程已经刷新过
                refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
                if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
                    log.info("{} AccessToken 未获取或已过期, 尝试刷新.", getStorageSimpleInfo());
                    refreshAccessToken();
                    refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
                }
            }
        }

        if (refreshTokenInfo == null) {
            throw new SystemException("存储源 " + storageId + " AccessToken 刷新失败: 未找到刷新令牌信息.");
        }

        return refreshTokenInfo.getData().getAccessToken();
    }

    private JSONObject sendRequest(String url, Method method, boolean withAuth, Map<String, Object> form, Map<String, List<String>> headers) {
        rateLimiter.acquire();

        HttpRequest httpRequest = HttpUtil.createRequest(method, url)
                .header(headers, true)
                .form(form);

        if (withAuth) {
            httpRequest.bearerAuth(checkExpiredAndGetAccessToken());
        }

        HttpResponse httpResponse = httpRequest.execute();
        return handleEntity(httpResponse);
    }

    private JSONObject sendPostRequestWithAuth(String url, Map<String, Object> form) {
        return sendRequest(url, Method.POST, true, form, null);
    }

    private JSONObject sendGetRequestWithAuth(String url, Map<String, Object> form) {
        return sendRequest(url, Method.GET, true, form, null);
    }

    private static JSONObject handleEntity(HttpResponse httpResponse) {
        if (!httpResponse.isOk()) {
            throw new SystemException("请求失败, 状态码: " + httpResponse.getStatus() + ", 响应体: " + httpResponse.body());
        }

        String responseBody = httpResponse.body();
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        if (jsonObject == null) {
            throw new SystemException("请求失败, 响应体解析失败: " + responseBody);
        }
        Boolean state = jsonObject.getBoolean("state");
        if (state == null || !state) {
            String message = jsonObject.getString("message");
            String code = jsonObject.getString("code");
            throw new SystemException("请求失败, 响应消息: " + message + ", 响应码: " + code + ", 详见: https://www.yuque.com/115yun/open/rnq0cbz8tt7cu43i");
        }

        return jsonObject;
    }

    /**
     * 将微软接口返回的 JSON 对象转为 FileItemResult 对象
     *
     * @param jsonObject    JSON 对象
     * @return              FileItemResult 对象
     */
    private FileItemResult listJsonToFileItem(JSONObject jsonObject, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(jsonObject.getString("fn"));
        fileItemResult.setSize(jsonObject.getLong("fs"));
        fileItemResult.setTime(new Date(jsonObject.getLong("upt") * 1000));

        if (Objects.equals(jsonObject.getString("fc"), FC_FILE)) {
            fileItemResult.setType(FileTypeEnum.FILE);
            String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath, fileItemResult.getName());
            idCacheService.putFileId(fullPath, jsonObject.getString("fid"));

            String pickCode = jsonObject.getString("pc");
            fileItemResult.setUrl(getProxyDownloadUrlByPickCode(StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName()), pickCode));
        } else {
            fileItemResult.setType(FileTypeEnum.FOLDER);
            String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath, fileItemResult.getName());
            idCacheService.putPathId(fullPath, jsonObject.getString("fid"));
        }
        fileItemResult.setPath(folderPath);
        return fileItemResult;
    }

    /**
     * 将微软接口返回的 JSON 对象转为 FileItemResult 对象
     *
     * @param jsonObject    JSON 对象
     * @return              FileItemResult 对象
     */
    private FileItemResult itemJsonToFileItem(JSONObject jsonObject, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(jsonObject.getString("file_name"));
        // 字符型转为 Long 字节型
        String size = jsonObject.getString("size");
        fileItemResult.setSize(FileSizeConverter.convertFileSizeToBytes(size));
        fileItemResult.setTime(new Date(jsonObject.getLong("utime") * 1000));

        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath, fileItemResult.getName());

        if (Objects.equals(jsonObject.getString("file_category"), FC_FILE)) {
            fileItemResult.setType(FileTypeEnum.FILE);
            idCacheService.putFileId(fullPath, jsonObject.getString("file_id"));

            String pickCode = jsonObject.getString("pick_code");
            fileItemResult.setUrl(getProxyDownloadUrlByPickCode(fullPath, pickCode));
        } else {
            fileItemResult.setType(FileTypeEnum.FOLDER);
            idCacheService.putPathId(fullPath, jsonObject.getString("file_id"));
        }
        fileItemResult.setPath(folderPath);
        return fileItemResult;
    }

}