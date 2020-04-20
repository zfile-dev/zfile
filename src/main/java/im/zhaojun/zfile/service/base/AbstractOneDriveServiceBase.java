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
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Zhao Jun
 */
@Slf4j
public abstract class AbstractOneDriveServiceBase extends AbstractBaseFileService {

    protected static final String DRIVER_INFO_URL = "https://{graphEndPoint}/v1.0/me/drives";

    protected static final String DRIVER_ROOT_URL = "https://{graphEndPoint}/v1.0/me/drive/root/children";

    protected static final String DRIVER_ITEMS_URL = "https://{graphEndPoint}/v1.0/me/drive/root:{path}:/children";

    protected static final String DRIVER_ITEM_URL = "https://{graphEndPoint}/v1.0/me/drive/root:{path}";

    protected static final String AUTHENTICATE_URL = "https://{authenticateEndPoint}/common/oauth2/v2.0/token";

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
                root = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), fullPath).getBody();
            } catch (HttpClientErrorException e) {
                log.debug("调用 OneDrive 时出现了网络异常: {} , 已尝试重新刷新 token 后再试.", e.getMessage());
                refreshOneDriveToken();
                root = oneDriveRestTemplate.exchange(requestUrl, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), fullPath).getBody();
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

        String requestUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.set("driveId", driveId.toString());
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        JSONObject fileItem = oneDriveRestTemplate.exchange(DRIVER_ITEM_URL, HttpMethod.GET, entity, JSONObject.class, getGraphEndPoint(), fullPath).getBody();

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


    public abstract String getGraphEndPoint();

    public abstract String getAuthenticateEndPoint();

    public abstract String getClientId();

    public abstract String getRedirectUri();

    public abstract String getClientSecret();

    public abstract String getScope();

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

    @Override
    public List<StorageConfig> storageStrategyConfigList() {
        return new ArrayList<StorageConfig>() {{
            add(new StorageConfig("accessToken", "访问令牌"));
            add(new StorageConfig("refreshToken", "刷新令牌"));
            add(new StorageConfig("basePath", "基路径"));
        }};
    }

}