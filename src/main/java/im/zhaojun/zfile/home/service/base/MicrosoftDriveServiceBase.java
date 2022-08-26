package im.zhaojun.zfile.home.service.base;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import im.zhaojun.zfile.admin.constant.StorageConfigConstant;
import im.zhaojun.zfile.admin.model.dto.OAuth2Token;
import im.zhaojun.zfile.admin.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.admin.model.param.MicrosoftDriveParam;
import im.zhaojun.zfile.admin.service.StorageSourceConfigService;
import im.zhaojun.zfile.common.cache.RefreshTokenCache;
import im.zhaojun.zfile.common.constant.ZFileConstant;
import im.zhaojun.zfile.common.exception.StorageSourceRefreshTokenException;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
public abstract class MicrosoftDriveServiceBase<P extends MicrosoftDriveParam> extends AbstractBaseFileService<P> implements RefreshTokenService {

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
    public OAuth2Token getRefreshToken() {
        StorageSourceConfig refreshStorageSourceConfig =
                storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);

        String param = "client_id=" + getClientId() +
                "&redirect_uri=" + getRedirectUri() +
                "&client_secret=" + getClientSecret() +
                "&refresh_token=" + refreshStorageSourceConfig.getValue() +
                "&grant_type=refresh_token";
    
        log.info("{} 尝试刷新令牌, 参数信息为: {}", this, param);
    
        String fullAuthenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpRequest post = HttpUtil.createPost(fullAuthenticateUrl);

        post.body(param, "application/x-www-form-urlencoded");
        HttpResponse response = post.execute();
        String body = response.body();
        log.info("{} 尝试刷新令牌成功, 响应信息为: {}", this, body);
        
        JSONObject jsonBody = JSONObject.parseObject(body);
        
        if (response.getStatus() != HttpStatus.OK.value()) {
            return OAuth2Token.fail(getClientId(), getClientSecret(), getRedirectUri(), body);
        }
        
        String accessToken = jsonBody.getString("access_token");
        String refreshToken = jsonBody.getString("refresh_token");
        return OAuth2Token.success(getClientId(), getClientSecret(), getRedirectUri(), accessToken, refreshToken, body);
    }

    /**
     * OAuth2 协议中, 根据 code 换取 access_token 和 refresh_token.
     *
     * @param   code
     *          代码
     *
     * @return  获取的 Token 信息.
     */
    public OAuth2Token getToken(String code, String clientId, String clientSecret, String redirectUri) {
        log.info("{} 根据授权回调 code 获取令牌：code: {}, clientId: {}, clientSecret: {}, redirectUri: {}", this, code, clientId, clientSecret, redirectUri);
        String param = "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&scope=" + getScope() +
                "&grant_type=authorization_code";

        String fullAuthenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpRequest post = HttpUtil.createPost(fullAuthenticateUrl);

        post.body(param, "application/x-www-form-urlencoded");
        HttpResponse response = post.execute();
        String body = response.body();
        log.info("{} 根据授权回调 code 获取令牌结果：body: {}", this, body);
        JSONObject jsonBody = JSONObject.parseObject(body);
    
        if (response.getStatus() != HttpStatus.OK.value()) {
            return OAuth2Token.fail(clientId, clientSecret, redirectUri, body);
        }
    
        String accessToken = jsonBody.getString("access_token");
        String refreshToken = jsonBody.getString("refresh_token");
        return OAuth2Token.success(clientId, clientSecret, redirectUri, accessToken, refreshToken, body);
    }

    @Override
    public List<FileItemResult> fileList(String folderPath) {
        folderPath = StringUtils.trimStartSlashes(folderPath);
        String fullPath = StringUtils.concat(param.getBasePath(), folderPath);

        List<FileItemResult> result = new ArrayList<>();
        String nextLink = null;

        do {
            String requestUrl;

            if (nextLink != null) {
                nextLink = nextLink.replace("+", "%2B");
                requestUrl = URLUtil.decode(nextLink);
            }else if (ZFileConstant.PATH_SEPARATOR.equalsIgnoreCase(fullPath) || "".equalsIgnoreCase(fullPath)) {
                requestUrl = DRIVER_ROOT_URL;
            } else {
                requestUrl = DRIVER_ITEMS_URL;
            }
            fullPath = StringUtils.trimEndSlashes(fullPath);

            JSONObject root;

            HttpHeaders headers = new HttpHeaders();
            headers.set("storageId", storageId.toString());
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            try {
                root = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
            } catch (HttpClientErrorException e) {
                log.debug("调用 OneDrive 时出现了网络异常, 响应信息: {}, 已尝试重新刷新 token 后再试.", e.getResponseBodyAsString());
                refreshAccessToken();
                root = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
            }

            if (root == null) {
                return Collections.emptyList();
            }

            nextLink = root.getString("@odata.nextLink");

            JSONArray fileList = root.getJSONArray("value");

            for (int i = 0; i < fileList.size(); i++) {
                JSONObject fileItem = fileList.getJSONObject(i);
                FileItemResult fileItemResult =jsonToFileItem(fileItem, folderPath);
                result.add(fileItemResult);
            }
        } while (nextLink != null);

        return result;
    }

    @Override
    public FileItemResult getFileItem(String pathAndName) {
        String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("storageId", storageId.toString());
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        JSONObject fileItem;

        try {
            fileItem = oneDriveRestTemplate.exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        } catch (HttpClientErrorException e) {
            log.debug("调用 OneDrive 时出现了网络异常, 响应信息: {}, 已尝试重新刷新 token 后再试.", e.getResponseBodyAsString());
            refreshAccessToken();
            fileItem = oneDriveRestTemplate.exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        }

        if (fileItem == null) {
            return null;
        }

        String folderPath = StringUtils.getParentPath(pathAndName);
        return jsonToFileItem(fileItem, folderPath);
    }


    @Override
    public boolean newFolder(String path, String name) {
        path = StringUtils.trimStartSlashes(path);
        String fullPath = StringUtils.concat(param.getBasePath(), path);

        String requestUrl;

        if (ZFileConstant.PATH_SEPARATOR.equalsIgnoreCase(fullPath) || "".equalsIgnoreCase(fullPath)) {
            requestUrl = DRIVER_ROOT_URL;
        } else {
            requestUrl = DRIVER_ITEMS_URL;
        }

        fullPath = StringUtils.trimEndSlashes(fullPath);

        JSONObject result = null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("storageId", storageId.toString());
        HashMap<Object, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("folder", new HashMap<>());
        data.put("@microsoft.graph.conflictBehavior", "replace");
        HttpEntity<Object> entity = new HttpEntity<>(data, headers);

        try {
            result = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.POST, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        } catch (Exception e) {
            log.error("存储源 {} 新建文件 {} 失败", storageId, fullPath, e);
        }

        return result != null;
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        HttpHeaders headers = new HttpHeaders();
        headers.set("storageId", storageId.toString());
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        try {
            oneDriveRestTemplate.exchange(DRIVER_ITEM_OPERATOR_URL, HttpMethod.DELETE, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        } catch (Exception e) {
            log.error("存储源 {} 删除文件 {} 失败", storageId, fullPath, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        HttpHeaders headers = new HttpHeaders();
        headers.set("storageId", storageId.toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", newName);
        HttpEntity<Object> entity = new HttpEntity<>(jsonObject, headers);

        try {
            oneDriveRestTemplate.exchange(DRIVER_ITEM_OPERATOR_URL, HttpMethod.PATCH, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        } catch (Exception e) {
            log.error("存储源 {} 重命名文件 {} 至 {} 失败", storageId, fullPath, newName, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }


    @Override
    public String getUploadUrl(String path, String name, Long size) {
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);

        HttpHeaders headers = new HttpHeaders();
        headers.set("storageId", storageId.toString());
        HttpEntity<Object> entity = new HttpEntity<>(headers);

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
            OAuth2Token refreshToken = getRefreshToken();

            if (refreshToken.getAccessToken() == null || refreshToken.getRefreshToken() == null) {
                throw new StorageSourceRefreshTokenException("获取或刷新 AccessToken 失败, 获取到的令牌为空, 相关诊断信息为: " + refreshToken, storageId);
            }

            StorageSourceConfig accessTokenConfig =
                    storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);
            StorageSourceConfig refreshTokenConfig =
                    storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.REFRESH_TOKEN_KEY);
            accessTokenConfig.setValue(refreshToken.getAccessToken());
            refreshTokenConfig.setValue(refreshToken.getRefreshToken());

            storageSourceConfigService.updateStorageConfig(Arrays.asList(accessTokenConfig, refreshTokenConfig));
            RefreshTokenCache.putRefreshTokenInfo(storageId, RefreshTokenCache.RefreshTokenInfo.success());
            log.info("存储源 {} 刷新 AccessToken 成功", storageId);
        } catch (Exception e) {
            RefreshTokenCache.putRefreshTokenInfo(storageId, RefreshTokenCache.RefreshTokenInfo.fail(getStorageTypeEnum().getDescription() + " AccessToken 刷新失败: " + e.getMessage()));
            throw new StorageSourceRefreshTokenException("存储源 ID: [{}] 刷新 AccessToken 失败", e, storageId);
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

}