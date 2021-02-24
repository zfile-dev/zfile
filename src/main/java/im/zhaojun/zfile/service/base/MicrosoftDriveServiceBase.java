package im.zhaojun.zfile.service.base;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.model.support.OneDriveToken;
import im.zhaojun.zfile.repository.StorageConfigRepository;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class MicrosoftDriveServiceBase extends AbstractBaseFileService {


    /**
     * https://graph.microsoft.com/v1.0/drives/6e4cf6d2f7e15197/root:%2FData%2F%E5%B8%A6%E6%9C%89%E7%AC%A6%E5%8F%B7%E6%96%87%E4%BB%B6%E5%A4%B9%E6%B5%8B%E8%AF%95%2F%25100+%2520:/children
     * https://graph.microsoft.com/v1.0/me/drive/root:%2FData%2F%E5%B8%A6%E6%9C%89%E7%AC%A6%E5%8F%B7%E6%96%87%E4%BB%B6%E5%A4%B9%E6%B5%8B%E8%AF%95%2F%25100+%2520:/children
     */

    /**
     * 获取根文件 API URI
     */
    protected static final String DRIVER_ROOT_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root/children";

    /**
     * 获取非根文件 API URI
     */
    protected static final String DRIVER_ITEMS_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}:/children";

    /**
     * 获取单文件 API URI
     */
    protected static final String DRIVER_ITEM_URL = "https://{graphEndPoint}/v1.0/{type}/drive/root:{path}";

    /**
     * 根据 RefreshToken 获取 AccessToken API URI
     */
    protected static final String AUTHENTICATE_URL = "https://{authenticateEndPoint}/common/oauth2/v2.0/token";

    /**
     * OneDrive 文件类型
     */
    private static final String ONE_DRIVE_FILE_FLAG = "file";


    @Resource
    @Lazy
    private RestTemplate oneDriveRestTemplate;

    @Resource
    private StorageConfigRepository storageConfigRepository;

    @Resource
    private StorageConfigService storageConfigService;

    /**
     * 根据 RefreshToken 刷新 AccessToken, 返回刷新后的 Token.
     *
     * @return  刷新后的 Token
     */
    public OneDriveToken getRefreshToken() {
        StorageConfig refreshStorageConfig =
                storageConfigRepository.findByDriveIdAndKey(driveId, StorageConfigConstant.REFRESH_TOKEN_KEY);

        String param = "client_id=" + getClientId() +
                "&redirect_uri=" + getRedirectUri() +
                "&client_secret=" + getClientSecret() +
                "&refresh_token=" + refreshStorageConfig.getValue() +
                "&grant_type=refresh_token";

        String fullAuthenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpRequest post = HttpUtil.createPost(fullAuthenticateUrl);

        post.body(param, "application/x-www-form-urlencoded");
        HttpResponse response = post.execute();
        return JSONObject.parseObject(response.body(), OneDriveToken.class);
    }

    /**
     * OAuth2 协议中, 根据 code 换取 access_token 和 refresh_token.
     *
     * @param   code
     *          代码
     *
     * @return  获取的 Token 信息.
     */
    public OneDriveToken getToken(String code) {
        String param = "client_id=" + getClientId() +
                "&redirect_uri=" + getRedirectUri() +
                "&client_secret=" + getClientSecret() +
                "&code=" + code +
                "&scope=" + getScope() +
                "&grant_type=authorization_code";

        String fullAuthenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getAuthenticateEndPoint());
        HttpRequest post = HttpUtil.createPost(fullAuthenticateUrl);

        post.body(param, "application/x-www-form-urlencoded");
        HttpResponse response = post.execute();
        return JSONObject.parseObject(response.body(), OneDriveToken.class);
    }

    @Override
    public List<FileItemDTO> fileList(String path) {
        path = StringUtils.removeFirstSeparator(path);
        String fullPath = StringUtils.getFullPath(basePath, path);

        List<FileItemDTO> result = new ArrayList<>();
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
            fullPath = StringUtils.removeLastSeparator(fullPath);

            JSONObject root;

            HttpHeaders headers = new HttpHeaders();
            headers.set("driveId", driveId.toString());
            HttpEntity<Object> entity = new HttpEntity<>(headers);

            try {
                root = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
            } catch (HttpClientErrorException e) {
                log.debug("调用 OneDrive 时出现了网络异常, 已尝试重新刷新 token 后再试.");
                refreshOneDriveToken();
                root = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
            }

            if (root == null) {
                return Collections.emptyList();
            }

            nextLink = root.getString("@odata.nextLink");

            JSONArray fileList = root.getJSONArray("value");

            for (int i = 0; i < fileList.size(); i++) {

                FileItemDTO fileItemDTO = new FileItemDTO();
                JSONObject fileItem = fileList.getJSONObject(i);
                fileItemDTO.setName(fileItem.getString("name"));
                fileItemDTO.setSize(fileItem.getLong("size"));
                fileItemDTO.setTime(fileItem.getDate("lastModifiedDateTime"));

                if (fileItem.containsKey("file")) {
                    fileItemDTO.setUrl(fileItem.getString("@microsoft.graph.downloadUrl"));
                    fileItemDTO.setType(FileTypeEnum.FILE);
                } else {
                    fileItemDTO.setType(FileTypeEnum.FOLDER);
                }

                fileItemDTO.setPath(path);
                result.add(fileItemDTO);
            }
        } while (nextLink != null);

        return result;
    }

    @Override
    public FileItemDTO getFileItem(String path) {

        String fullPath = StringUtils.getFullPath(basePath, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("driveId", driveId.toString());
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        JSONObject fileItem;

        try {
            fileItem = oneDriveRestTemplate.exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        } catch (HttpClientErrorException e) {
            log.debug("调用 OneDrive 时出现了网络异常, 已尝试重新刷新 token 后再试.");
            refreshOneDriveToken();
            fileItem = oneDriveRestTemplate.exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), getType(), fullPath).getBody();
        }

        if (fileItem == null) {
            return null;
        }

        FileItemDTO fileItemDTO = new FileItemDTO();
        fileItemDTO.setName(fileItem.getString("name"));
        fileItemDTO.setSize(fileItem.getLong("size"));
        fileItemDTO.setTime(fileItem.getDate("lastModifiedDateTime"));

        if (fileItem.containsKey(ONE_DRIVE_FILE_FLAG)) {
            fileItemDTO.setUrl(fileItem.getString("@microsoft.graph.downloadUrl"));
            fileItemDTO.setType(FileTypeEnum.FILE);
        } else {
            fileItemDTO.setType(FileTypeEnum.FOLDER);
        }

        fileItemDTO.setPath(path);
        return fileItemDTO;
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
     * 刷新当前存储器 AccessToken
     */
    public void refreshOneDriveToken() {
        OneDriveToken refreshToken = getRefreshToken();

        if (refreshToken.getAccessToken() == null || refreshToken.getRefreshToken() == null) {
            return;
        }

        StorageConfig accessTokenConfig =
                storageConfigService.findByDriveIdAndKey(driveId, StorageConfigConstant.ACCESS_TOKEN_KEY);
        StorageConfig refreshTokenConfig =
                storageConfigService.findByDriveIdAndKey(driveId, StorageConfigConstant.REFRESH_TOKEN_KEY);
        accessTokenConfig.setValue(refreshToken.getAccessToken());
        refreshTokenConfig.setValue(refreshToken.getRefreshToken());

        storageConfigService.updateStorageConfig(Arrays.asList(accessTokenConfig, refreshTokenConfig));
    }

}
