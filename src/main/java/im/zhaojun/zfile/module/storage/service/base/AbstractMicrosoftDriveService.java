package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import im.zhaojun.zfile.core.exception.system.UploadFileFailSystemException;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageConfigConstant;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.dto.RefreshTokenInfoDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.MicrosoftDriveParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.oauth2.service.IOAuth2Service;
import im.zhaojun.zfile.module.storage.service.StorageSourceConfigService;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractMicrosoftDriveService<P extends MicrosoftDriveParam> extends AbstractProxyTransferService<P> implements RefreshTokenService {

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    /**
     * 获取根文件 API URI
     */
    protected static final String DRIVER_ROOT_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root/children?select=name,size,lastModifiedDateTime,file,@microsoft.graph.downloadUrl,@odata.nextLink,value";

    /**
     * 获取非根文件 API URI
     */
    protected static final String DRIVER_ITEMS_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}:/children?select=name,size,lastModifiedDateTime,file,@microsoft.graph.downloadUrl,@odata.nextLink,value";

    /**
     * 获取单文件 API URI
     */
    protected static final String DRIVER_ITEM_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}?select=name,size,lastModifiedDateTime,file,@microsoft.graph.downloadUrl,id";

    /**
     * 操作单文件 API URI
     */
    protected static final String DRIVER_ITEM_OPERATOR_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}";

    /**
     * 根据 RefreshToken 获取 AccessToken API URI
     */
    protected static final String AUTHENTICATE_URL = "https://{authenticateEndPoint}/common/oauth2/v2.0/token";

    /**
     * 创建上传文件回话 API
     */
    protected static final String CREATE_UPLOAD_SESSION_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}:/createUploadSession";

    /**
     * 复制文件 API
     */
    private static final String DRIVER_COPY_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}:/copy";

    /**
     * OneDrive 文件类型
     */
    private static final String ONE_DRIVE_FILE_FLAG = "file";

    /*
     * 设置 RestTemplate 使用 Netty 底层实现，默认的实现不支持 PATCH 请求
     */
    private volatile RestTemplate restTemplate;

    @Override
    public void init() {
        Integer refreshTokenExpiredAt = param.getRefreshTokenExpiredAt();
        if (refreshTokenExpiredAt == null) {
            try {
                JWT jwt = JWTUtil.parseToken(param.getAccessToken());
                JWTPayload payload = jwt.getPayload();
                refreshTokenExpiredAt = Convert.toInt(payload.getClaim("exp"));
                if (log.isDebugEnabled()) {
                    log.debug("初始化时尝试根据 AccessToken 自动解析到期时间: {}", refreshTokenExpiredAt);
                }
            } catch (Exception e) {
                log.warn("初始化时尝试根据 AccessToken 自动解析到期时间异常", e);
            }
        }

        if (refreshTokenExpiredAt == null) {
            refreshAccessToken();
        } else {
            RefreshTokenInfoDTO tokenInfoDTO = RefreshTokenInfoDTO.success(param.getAccessToken(), param.getRefreshToken(), refreshTokenExpiredAt);
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));
        }
    }

    public RestTemplate getRestTemplate() {
        // 双重检查锁，避免重复创建 RestTemplate 实例的同时减少锁的开销
        if (restTemplate == null) {
            synchronized (this) {
                if (restTemplate == null) {
                    restTemplate = new RestTemplate();
                    int timeoutSecond = param.getProxyUploadTimeoutSecond() == null ? 0 : param.getProxyUploadTimeoutSecond();
                    RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(timeoutSecond)).build();
                    HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig((requestConfig)).build();
                    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
                    restTemplate.setRequestFactory(requestFactory);
                }
            }
        }
        return restTemplate;
    }


    @Override
    public List<FileItemResult> fileList(String folderPath) {
        String fullPath = StringUtils.concatTrimEndSlashes(param.getBasePath(), getCurrentUserBasePath(), folderPath);

        List<FileItemResult> result = new ArrayList<>();
        String nextPageLink = null;

        do {
            String requestUrl;

            // 如果有下一页链接，则优先取下一页
            // 如果没有则判断是根目录还是子目录
            if (nextPageLink != null) {
                nextPageLink = nextPageLink.replace("+", "%2B");
                requestUrl = URLUtil.decode(nextPageLink);
            } else if (StringUtils.SLASH.equalsIgnoreCase(fullPath) || "".equalsIgnoreCase(fullPath)) {
                requestUrl = DRIVER_ROOT_URL;
            } else {
                requestUrl = DRIVER_ITEMS_URL;
            }

            HttpEntity<Object> entity = getAuthorizationHttpEntity();
            JSONObject root = getRestTemplate().exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
            if (root == null) {
                return Collections.emptyList();
            }

            JSONArray fileList = root.getJSONArray("value");
            for (int i = 0; i < fileList.size(); i++) {
                JSONObject fileItem = fileList.getJSONObject(i);
                FileItemResult fileItemResult = jsonToFileItem(fileItem, folderPath);
                if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getProxyDomain())) {
                    fileItemResult.setUrl(getProxyDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName())));
                }
                result.add(fileItemResult);
            }
    
            nextPageLink = root.getString("@odata.nextLink");
        } while (nextPageLink != null);
    
        return result;
    }
    
    @Override
    public FileItemResult getFileItem(String pathAndName) {
        String fullPath = StringUtils.concat(getCurrentUserBasePath(), pathAndName);
        return getOriginFileItem(fullPath);
    }


    /**
     * 获取原始的 FileItem 信息，尚未按照存储源参数设置代理下载地址
     */
    public FileItemResult getOriginFileItem(String pathAndName) {
        JSONObject fileItem = getFileOriginInfo(pathAndName);
        if (fileItem == null) return null;

        String folderPath = FileUtils.getParentPath(pathAndName);
        return jsonToFileItem(fileItem, folderPath);
    }

    @Nullable
    private JSONObject getFileOriginInfo(String pathAndName) {
        String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);
        HttpEntity<Object> entity = getAuthorizationHttpEntity();
        return getRestTemplate().exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
    }


    @Override
    public boolean newFolder(String path, String name) {
        path = StringUtils.trimStartSlashes(path);
        String fullPath = StringUtils.concatTrimEndSlashes(param.getBasePath(), getCurrentUserBasePath(), path);

        String requestUrl;

        if (StringUtils.SLASH.equalsIgnoreCase(fullPath) || "".equalsIgnoreCase(fullPath)) {
            requestUrl = DRIVER_ROOT_URL;
        } else {
            requestUrl = DRIVER_ITEMS_URL;
        }

        HashMap<Object, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("folder", new HashMap<>());
        data.put("@microsoft.graph.conflictBehavior", "replace");

        HttpEntity<HashMap<Object, Object>> entity = getAuthorizationHttpEntity(data);
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(requestUrl, HttpMethod.POST, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);

        HttpEntity<Object> entity = getAuthorizationHttpEntity();
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(DRIVER_ITEM_OPERATOR_URL, HttpMethod.DELETE, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);

        JSONObject jsonObject = new JSONObject().fluentPut("name", newName);

        HttpEntity<Object> entity = getAuthorizationHttpEntity(jsonObject);
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(DRIVER_ITEM_OPERATOR_URL, HttpMethod.PATCH, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        if (param.isEnableProxyUpload()) {
            return super.getProxyUploadUrl(path, name);
        }
        return getOneDriveUploadUrl(StringUtils.concat(getCurrentUserBasePath(), path), name);
    }

    private String getOneDriveUploadUrl(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        HttpEntity<Object> entity = getAuthorizationHttpEntity();
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(CREATE_UPLOAD_SESSION_URL,
                HttpMethod.POST, entity, JSONObject.class,
                getGraphEndPoint(), getType(), fullPath);

        JSONObject responseEntityBody = responseEntity.getBody();
        if (responseEntityBody == null) {
            throw new SystemException("获取上传地址失败, 返回值为空.");
        }
        return responseEntityBody.getString("uploadUrl");
    }

    @Override
    public void uploadFile(String pathAndName, InputStream inputStream, Long size) throws IOException {
        String fullPath = StringUtils.concat(getCurrentUserBasePath(), pathAndName);
        String folderPath = FileUtils.getParentPath(fullPath);
        String fileName = FileUtils.getName(fullPath);
        String uploadUrl = getOneDriveUploadUrl(folderPath, fileName);

        try {
            getRestTemplate().execute(uploadUrl, HttpMethod.PUT, request -> {
                HttpHeaders headers = request.getHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(size);
                headers.set(HttpHeaders.CONTENT_RANGE, "bytes 0-" + (size - 1) + StringUtils.SLASH + size);
                StreamUtils.copy(inputStream, request.getBody());
            }, clientHttpResponse -> {
                if (!clientHttpResponse.getStatusCode().is2xxSuccessful()) {
                    throw new UploadFileFailSystemException(this.getStorageTypeEnum(), pathAndName, size,
                            clientHttpResponse.getStatusCode().value(), clientHttpResponse.getStatusText());
                }
                return null;
            });
        } catch (Exception e) {
            if (e instanceof ResourceAccessException && e.getMessage() != null && e.getMessage().contains("Timeout on")) {
                throw new BizException(ErrorCode.BIZ_UPLOAD_FILE_TIMEOUT_ERROR);
            }
            throw new UploadFileFailSystemException(this.getStorageTypeEnum(), pathAndName, size, 500, e.getMessage(), e);
        }
    }
    @Override
    public String getDownloadUrl(String pathAndName) {
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getProxyDomain())) {
            return getProxyDownloadUrl(pathAndName);
        }
        FileItemResult fileItem = getFileItem(pathAndName);
        if (fileItem == null) {
            throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }
        return fileItem.getUrl();
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadToStream(String pathAndName) throws IOException {
        FileItemResult fileItem = getOriginFileItem(pathAndName);
        if (fileItem == null) {
            throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }
        Long fileSize = fileItem.getSize();
        String fileName = fileItem.getName();
        String url = fileItem.getUrl();

        // url 转换为 inputStream
        getRestTemplate().execute(url, HttpMethod.GET, null, clientHttpResponse -> {
            InputStream inputStream = clientHttpResponse.getBody();
            RequestHolder.writeFile(inputStream, fileName, fileSize, false, param.isProxyLinkForceDownload());
            return null;
        });
        return null;
    }

    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String targetFullPath = StringUtils.concat(getCurrentUserBasePath(), targetPath);

        JSONObject fileOriginInfo = getFileOriginInfo(targetFullPath);
        if (fileOriginInfo == null) {
            throw new BizException(ErrorCode.BIZ_FOLDER_NOT_EXIST);
        }

        String targetPathId = fileOriginInfo.getString("id");
        JSONObject jsonObject = new JSONObject()
                .fluentPut("name", targetName)
                .fluentPut("parentReference", new JSONObject().fluentPut("id", targetPathId))
                .fluentPut("@microsoft.graph.conflictBehavior", "replace");

        HttpEntity<Object> entity = getAuthorizationHttpEntity(jsonObject);
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(DRIVER_COPY_URL,
                                                                                    HttpMethod.POST,
                                                                                    entity,
                                                                                    JSONObject.class,
                                                                                    getGraphEndPoint(),
                                                                                    getType(),
                                                                                    fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        return copyFile(path, name, targetPath, targetName);
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String targetFullPath = StringUtils.concat(getCurrentUserBasePath(), targetPath);

        JSONObject fileOriginInfo = getFileOriginInfo(targetFullPath);
        if (fileOriginInfo == null) {
            throw new BizException(ErrorCode.BIZ_FOLDER_NOT_EXIST);
        }

        String targetPathId = fileOriginInfo.getString("id");

        JSONObject jsonObject = new JSONObject()
                .fluentPut("name", targetName)
                .fluentPut("parentReference", new JSONObject().fluentPut("id", targetPathId))
                .fluentPut("@microsoft.graph.conflictBehavior", "replace");

        HttpEntity<Object> entity = getAuthorizationHttpEntity(jsonObject);
        ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(DRIVER_ITEM_OPERATOR_URL,
                                                                                    HttpMethod.PATCH,
                                                                                    entity,
                                                                                    JSONObject.class,
                                                                                    getGraphEndPoint(),
                                                                                    getType(),
                                                                                    fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        return moveFile(path, name, targetPath, targetName);
    }

    /**
     * 获取存储类型, 对于 OneDrive 或 SharePoint, 此地址会不同.
     * @return          Graph 连接点
     */
    public abstract String getType();

    /**
     * 获取 GraphEndPoint, 对于不同版本的 OneDrive, 此地址会不同.
     * @return          Graph 连接点
     */
    public abstract String getGraphEndPoint();


    /**
     * 获取 AuthenticateEndPoint, 对于不同版本的 OneDrive, 此地址会不同.
     * @return          Authenticate 连接点
     */
    public abstract String getAuthenticateEndPoint();

    /**
     * 获取 Client ID.
     * @return  Client Id
     */
    public abstract String getClientId();

    /**
     * 获取重定向地址.
     * @return  重定向地址
     */
    public abstract String getRedirectUri();

    /**
     * 获取 Client Secret 密钥.
     * @return  Client Secret 密钥.
     */
    public abstract String getClientSecret();

    /**
     * 获取 API Scope.
     * @return  Scope
     */
    public abstract String getScope();


    /**
     * 刷新当前存储源 AccessToken
     */
    @Override
    public void refreshAccessToken() {
        try {
            RefreshTokenInfoDTO tokenInfoDTO = getAndRefreshToken();

            if (tokenInfoDTO.getAccessToken() == null || tokenInfoDTO.getRefreshToken() == null) {
                throw new SystemException("存储源 " + storageId + " 刷新令牌失败, 获取到令牌为空.");
            }

            StorageSourceConfig accessTokenConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
            StorageSourceConfig refreshTokenConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);
            StorageSourceConfig refreshTokenExpiredAtConfig = storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_EXPIRED_AT_KEY);
            accessTokenConfig.setValue(tokenInfoDTO.getAccessToken());
            refreshTokenConfig.setValue(tokenInfoDTO.getRefreshToken());
            refreshTokenExpiredAtConfig.setValue(String.valueOf(tokenInfoDTO.getExpiredAt()));

            storageSourceConfigService.updateBatch(storageId, Arrays.asList(accessTokenConfig, refreshTokenConfig, refreshTokenExpiredAtConfig));
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));
        } catch (Exception e) {
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.fail("AccessToken 刷新失败: " + e.getMessage()));
            throw new SystemException("存储源 " + storageId + " 刷新令牌失败, 获取时发生异常.", e);

        }
    }


    /**
     * 将微软接口返回的 JSON 对象转为 FileItemResult 对象
     *
     * @param jsonObject    JSON 对象
     * @param folderPath    文件夹路径
     * @return              FileItemResult 对象
     */
    private FileItemResult jsonToFileItem(JSONObject jsonObject, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(jsonObject.getString("name"));
        fileItemResult.setSize(jsonObject.getLong("size"));
        fileItemResult.setTime(jsonObject.getDate("lastModifiedDateTime"));

        if (jsonObject.containsKey(ONE_DRIVE_FILE_FLAG)) {
            String originUrl = jsonObject.getString("@microsoft.graph.downloadUrl");
            if (StringUtils.isNotEmpty(param.getProxyDomain())) {
                originUrl = StringUtils.replaceHost(originUrl, param.getProxyDomain());
            }

            fileItemResult.setUrl(originUrl);
            fileItemResult.setType(FileTypeEnum.FILE);
        } else {
            fileItemResult.setType(FileTypeEnum.FOLDER);
        }
        fileItemResult.setPath(folderPath);
        return fileItemResult;
    }
    
    
    /**
     * 获取存储源默认的 HttpEntity 对象.
     * <br>
     * 该对象默认包含了当前存储源的 AccessToken.
     *
     * @return  HttpEntity 对象
     */
    private HttpEntity<Object> getAuthorizationHttpEntity() {
       return getAuthorizationHttpEntity(null);
    }
    
    
    /**
     * 获取存储源默认的 HttpEntity 对象.
     * <br>
     * 该对象默认包含了当前存储源的 AccessToken.
     *
     * @param   body
     *          请求体
     *
     * @return  HttpEntity 对象
     */
    private <T> HttpEntity<T> getAuthorizationHttpEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = checkExpiredAndGetAccessToken();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    @Override
    public StorageSourceMetadata getStorageSourceMetadata() {
        StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
        if (param.isEnableProxyUpload()) {
            storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
        } else {
            storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.MICROSOFT);
        }
        storageSourceMetadata.setNeedCreateFolderBeforeUpload(false);
        return storageSourceMetadata;
    }

    /**
     * 根据 RefreshToken 刷新 AccessToken, 返回刷新后的 Token.
     *
     * @return  刷新后的 Token
     */
    private RefreshTokenInfoDTO getAndRefreshToken() {
        StorageSourceConfig refreshStorageSourceConfig =
                storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);

        String param = "client_id=" + getClientId() +
                "&redirect_uri=" + getRedirectUri() +
                "&client_secret=" + getClientSecret() +
                "&refresh_token=" + refreshStorageSourceConfig.getValue() +
                "&grant_type=refresh_token";

        if (log.isDebugEnabled()) {
            log.debug("{} 尝试刷新令牌, 请求参数: {}", getStorageSimpleInfo(), param);
        }

        String authenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpResponse response = HttpUtil.createPost(authenticateUrl)
                .body(param, ContentType.FORM_URLENCODED.getValue())
                .execute();

        String responseBody = response.body();
        int responseStatus = response.getStatus();

        if (log.isDebugEnabled()) {
            log.debug("{} 刷新令牌完成. 响应状态码: {}, 响应体: {}", getStorageSimpleInfo(), responseStatus, responseBody);
        }

        if (responseStatus != HttpStatus.OK.value()) {
            throw new SystemException(responseBody);
        }

        JSONObject jsonBody = JSONObject.parseObject(responseBody);
        String accessToken = jsonBody.getString(IOAuth2Service.ACCESS_TOKEN_FIELD_NAME);
        String refreshToken = jsonBody.getString(IOAuth2Service.REFRESH_TOKEN_FIELD_NAME);
        Integer expiresIn = jsonBody.getInteger(IOAuth2Service.EXPIRES_IN_FIELD_NAME);
        return RefreshTokenInfoDTO.success(accessToken, refreshToken, expiresIn);
    }

    @Override
    public void destroy() {
        if (restTemplate != null && restTemplate.getRequestFactory() instanceof CloseableHttpClient) {
            try {
                ((CloseableHttpClient) restTemplate.getRequestFactory()).close();
            } catch (IOException e) {
                log.error("关闭 {} 的 HTTP 客户端失败.", getStorageSimpleInfo(), e);
            }
        }
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
                    if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
                        log.info("{} AccessToken 未获取或已过期, 尝试刷新: {}", getStorageSimpleInfo(), refreshTokenInfo);
                        refreshAccessToken();
                        refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
                    }
                }
            }
        }

        if (refreshTokenInfo == null) {
            throw new SystemException("存储源 " + storageId + " AccessToken 刷新失败: 未找到刷新令牌信息.");
        }

        return refreshTokenInfo.getData().getAccessToken();
    }

}