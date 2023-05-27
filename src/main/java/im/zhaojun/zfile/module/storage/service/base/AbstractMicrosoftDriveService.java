package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.constant.ZFileConstant;
import im.zhaojun.zfile.core.exception.StorageSourceRefreshTokenException;
import im.zhaojun.zfile.core.exception.ZFileRetryException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageConfigConstant;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.MicrosoftDriveParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.StorageSourceConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractMicrosoftDriveService<P extends MicrosoftDriveParam> extends AbstractBaseFileService<P> implements RefreshTokenService {

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
    protected static final String DRIVER_ITEM_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}?select=name,size,lastModifiedDateTime,file,@microsoft.graph.downloadUrl";

    /**
     * 操作单文件 API URI
     */
    protected static final String DRIVER_ITEM_OPERATOR_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}";

    /**
     * 根据 RefreshToken 获取 AccessToken API URI
     */
    protected static final String AUTHENTICATE_URL = "https://{authenticateEndPoint}/common/oauth2/v2.0/token";

    /**
     * 搜索文件 API URI
     */
    protected static final String DRIVER_SEARCH_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root/search(q='{path}')";

    /**
     * 创建上传文件回话 API
     */
    protected static final String CREATE_UPLOAD_SESSION_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}:/createUploadSession";

    /**
     * OneDrive 文件类型
     */
    private static final String ONE_DRIVE_FILE_FLAG = "file";
    
    /**
     * 访问令牌字段名称
     */
    public static final String ACCESS_TOKEN_FIELD_NAME = "access_token";
    
    /**
     * 刷新令牌字段名称
     */
    public static final String REFRESH_TOKEN_FIELD_NAME = "refresh_token";
    
    
    @Resource
    @Lazy
    private RestTemplate oneDriveRestTemplate;

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    /**
     * 根据 RefreshToken 刷新 AccessToken, 返回刷新后的 Token.
     *
     * @return  刷新后的 Token
     */
    public OAuth2TokenDTO getAndRefreshToken() {
        StorageSourceConfig refreshStorageSourceConfig =
                storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);

        String param = "client_id=" + getClientId() +
                "&redirect_uri=" + getRedirectUri() +
                "&client_secret=" + getClientSecret() +
                "&refresh_token=" + refreshStorageSourceConfig.getValue() +
                "&grant_type=refresh_token";
    
        if (log.isDebugEnabled()) {
            log.debug("{} 尝试刷新令牌, 请求参数: [{}]", getStorageSimpleInfo(), param);
        }
    
        String authenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpResponse response = HttpUtil.createPost(authenticateUrl)
                .body(param, ContentType.FORM_URLENCODED.getValue())
                .execute();
    
        String responseBody = response.body();
        int responseStatus = response.getStatus();
    
        log.info("{} 刷新令牌完成. [httpStatus: {}]", getStorageSimpleInfo(), responseStatus);
        
        if (responseStatus != HttpStatus.OK.value()) {
            return OAuth2TokenDTO.fail(getClientId(), getClientSecret(), getRedirectUri(), responseBody);
        }
    
        JSONObject jsonBody = JSONObject.parseObject(responseBody);
        String accessToken = jsonBody.getString(ACCESS_TOKEN_FIELD_NAME);
        String refreshToken = jsonBody.getString(REFRESH_TOKEN_FIELD_NAME);
        return OAuth2TokenDTO.success(getClientId(), getClientSecret(), getRedirectUri(), accessToken, refreshToken, responseBody);
    }

    /**
     * OAuth2 协议中, 根据 code 换取 access_token 和 refresh_token.
     *
     * @param   code
     *          代码
     *
     * @return  获取的 Token 信息.
     */
    public OAuth2TokenDTO getTokenByCode(String code, String clientId, String clientSecret, String redirectUri) {
        String param = "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&scope=" + getScope() +
                "&grant_type=authorization_code";
    
        if (log.isDebugEnabled()) {
            log.debug("根据授权回调 code 获取存储源类型为 [{}] 的令牌, 请求参数: [{}]", this.getStorageTypeEnum().getDescription(), param);
        }
    
        String fullAuthenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpResponse response = HttpUtil.createPost(fullAuthenticateUrl)
                .body(param, "application/x-www-form-urlencoded")
                .execute();

        String responseBody = response.body();
        int responseStatus = response.getStatus();
        
        log.info("根据授权回调 code 获取存储源类型为 [{}] 的令牌完成. [httpStatus: {}]", this.getStorageTypeEnum().getDescription(), responseStatus);
    
        if (responseStatus != HttpStatus.OK.value()) {
            return OAuth2TokenDTO.fail(clientId, clientSecret, redirectUri, responseBody);
        }
    
        JSONObject jsonBody = JSONObject.parseObject(responseBody);
        String accessToken = jsonBody.getString(ACCESS_TOKEN_FIELD_NAME);
        String refreshToken = jsonBody.getString(REFRESH_TOKEN_FIELD_NAME);
        return OAuth2TokenDTO.success(clientId, clientSecret, redirectUri, accessToken, refreshToken, responseBody);
    }

    @Override
    public List<FileItemResult> fileList(String folderPath) {
        folderPath = StringUtils.trimStartSlashes(folderPath);
        String fullPath = StringUtils.concatTrimEndSlashes(param.getBasePath(), folderPath);

        List<FileItemResult> result = new ArrayList<>();
        String nextPageLink = null;

        do {
            String requestUrl;

            // 如果有下一页链接，则优先取下一页
            // 如果没有则判断是根目录还是子目录
            if (nextPageLink != null) {
                nextPageLink = nextPageLink.replace("+", "%2B");
                requestUrl = URLUtil.decode(nextPageLink);
            } else if (ZFileConstant.PATH_SEPARATOR.equalsIgnoreCase(fullPath) || "".equalsIgnoreCase(fullPath)) {
                requestUrl = DRIVER_ROOT_URL;
            } else {
                requestUrl = DRIVER_ITEMS_URL;
            }

            JSONObject root = executeRetryableRequest(context -> {
                int retryCount = context.getRetryCount();
                if (retryCount > 0) {
                    HttpClientErrorException ex = (HttpClientErrorException) context.getLastThrowable();
                    log.warn("{} 调用 OneDrive 列表时出现了网络异常, 响应信息: [{}], 将尝试重新刷新 token 后再试. 文件路径为: [{}]",
                            getStorageSimpleInfo(), ex.getResponseBodyAsString(), fullPath, ex);
                    refreshAccessToken();
                }
    
                HttpEntity<Object> entity = getAuthorizationHttpEntity();
                return oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
            });
    
            if (root == null) {
                return Collections.emptyList();
            }

            JSONArray fileList = root.getJSONArray("value");
            for (int i = 0; i < fileList.size(); i++) {
                JSONObject fileItem = fileList.getJSONObject(i);
                FileItemResult fileItemResult = jsonToFileItem(fileItem, folderPath);
                result.add(fileItemResult);
            }
    
            nextPageLink = root.getString("@odata.nextLink");
        } while (nextPageLink != null);
    
        return result;
    }
    
    @Override
    public FileItemResult getFileItem(String pathAndName) {
        String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);

        JSONObject fileItem = executeRetryableRequest(context -> {
            int retryCount = context.getRetryCount();
            if (retryCount > 0) {
                refreshAccessToken();
                HttpClientErrorException ex = (HttpClientErrorException) context.getLastThrowable();
                log.warn("{} 调用 OneDrive 获取文件信息时出现了网络异常, 响应信息: [{}], 将尝试重新刷新 token 后再试. 获取文件路径为: {}",
                        getStorageSimpleInfo(), ex.getResponseBodyAsString(), fullPath, ex);
        
            }
    
            HttpEntity<Object> entity = getAuthorizationHttpEntity();
            return oneDriveRestTemplate.exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        });

        if (fileItem == null) {
            return null;
        }

        String folderPath = StringUtils.getParentPath(pathAndName);
        return jsonToFileItem(fileItem, folderPath);
    }


    @Override
    public boolean newFolder(String path, String name) {
        path = StringUtils.trimStartSlashes(path);
        String fullPath = StringUtils.concatTrimEndSlashes(param.getBasePath(), path);

        String requestUrl;

        if (ZFileConstant.PATH_SEPARATOR.equalsIgnoreCase(fullPath) || "".equalsIgnoreCase(fullPath)) {
            requestUrl = DRIVER_ROOT_URL;
        } else {
            requestUrl = DRIVER_ITEMS_URL;
        }

        HashMap<Object, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("folder", new HashMap<>());
        data.put("@microsoft.graph.conflictBehavior", "replace");
    
        HttpEntity<HashMap<Object, Object>> entity = getAuthorizationHttpEntity(data);
        ResponseEntity<JSONObject> responseEntity = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.POST, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        HttpEntity<Object> entity = getAuthorizationHttpEntity();
        ResponseEntity<JSONObject> responseEntity = oneDriveRestTemplate.exchange(DRIVER_ITEM_OPERATOR_URL, HttpMethod.DELETE, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        JSONObject jsonObject = new JSONObject().fluentPut("name", newName);
    
        HttpEntity<Object> entity = getAuthorizationHttpEntity(jsonObject);
        ResponseEntity<JSONObject> responseEntity = oneDriveRestTemplate.exchange(DRIVER_ITEM_OPERATOR_URL, HttpMethod.PATCH, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        HttpEntity<Object> entity = getAuthorizationHttpEntity();
        ResponseEntity<JSONObject> responseEntity = oneDriveRestTemplate.exchange(CREATE_UPLOAD_SESSION_URL,
                    HttpMethod.POST, entity, JSONObject.class,
                    getGraphEndPoint(), getType(), fullPath);
       
        JSONObject responseEntityBody = responseEntity.getBody();
        return responseEntityBody.getString("uploadUrl");
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
            OAuth2TokenDTO refreshToken = getAndRefreshToken();

            if (refreshToken.getAccessToken() == null || refreshToken.getRefreshToken() == null) {
                throw new StorageSourceRefreshTokenException("存储源刷新令牌失败, 获取到令牌为空.", storageId);
            }

            StorageSourceConfig accessTokenConfig =
                    storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
            StorageSourceConfig refreshTokenConfig =
                    storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);
            accessTokenConfig.setValue(refreshToken.getAccessToken());
            refreshTokenConfig.setValue(refreshToken.getRefreshToken());

            storageSourceConfigService.updateBatch(storageId, Arrays.asList(accessTokenConfig, refreshTokenConfig));
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success());
        } catch (Exception e) {
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.fail(getStorageTypeEnum().getDescription() + " AccessToken 刷新失败: " + e.getMessage()));
            throw new StorageSourceRefreshTokenException("存储源刷新令牌失败，获取时发生异常", e, storageId);
        }
    }


    private FileItemResult jsonToFileItem(JSONObject jsonObject, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(jsonObject.getString("name"));
        fileItemResult.setSize(jsonObject.getLong("size"));
        fileItemResult.setTime(jsonObject.getDate("lastModifiedDateTime"));

        if (jsonObject.containsKey(ONE_DRIVE_FILE_FLAG)) {
            String originUrl = jsonObject.getString("@microsoft.graph.downloadUrl");
            if (StrUtil.isNotEmpty(param.getProxyDomain())) {
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
        StorageSourceConfig accessTokenConfig =
                storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
        headers.setBearerAuth(accessTokenConfig.getValue());
        return new HttpEntity<>(body, headers);
    }
    
    
    /**
     * 执行可重试 1 次的任务, 对抛出的异常转为 ZFileRetryException(Unchecked Exception)
     * @param retryCallback     可重试的任务
     * @return                  任务执行结果
     *
     * @param                   <T> 任务执行结果类型
     */
    private <T> T executeRetryableRequest(RetryCallback<T, Throwable> retryCallback) {
        RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(2).retryOn(HttpClientErrorException.class).build();
    
        T result;
        try {
            result = retryTemplate.execute(retryCallback);
        } catch (Throwable e) {
            throw new ZFileRetryException("请求失败", e);
        }
        
        return result;
    }

}