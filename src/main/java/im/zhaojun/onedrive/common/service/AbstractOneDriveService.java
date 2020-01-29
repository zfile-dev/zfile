package im.zhaojun.onedrive.common.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.repository.StorageConfigRepository;
import im.zhaojun.common.util.StringUtils;
import im.zhaojun.onedrive.common.model.OneDriveToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhao Jun
 * 2020/1/29 11:54
 */
public abstract class AbstractOneDriveService {

    protected static final String DRIVER_INFO_URL = "https://{graphEndPoint}/v1.0/me/drives";

    protected static final String DRIVER_ROOT_URL = "https://{graphEndPoint}/v1.0/me/drive/root/children";

    protected static final String DRIVER_ITEMS_URL = "https://{graphEndPoint}/v1.0/me/drive/root:{path}:/children";

    protected static final String DRIVER_ITEM_URL = "https://{graphEndPoint}/v1.0/me/drive/root:{path}";

    protected static final String AUTHENTICATE_URL = "https://{authenticateEndPoint}/common/oauth2/v2.0/token";

    @Resource
    private RestTemplate oneDriveRestTemplate;

    @Resource
    private StorageConfigRepository storageConfigRepository;

    public OneDriveToken getRefreshToken() {
        StorageConfig refreshStorageConfig =
                storageConfigRepository.findByTypeAndKey(StorageTypeEnum.ONE_DRIVE, StorageConfigConstant.REFRESH_TOKEN_KEY);

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



    public String getUserInfo() {
        return oneDriveRestTemplate.getForObject(DRIVER_INFO_URL, String.class);
    }

    public List<FileItemDTO> list(String path) {
        List<FileItemDTO> result = new ArrayList<>();
        String nextLink = null;

        do {

            String requestUrl;

            if (nextLink != null) {
                requestUrl = nextLink;
            }else if ("/".equalsIgnoreCase(path)) {
                requestUrl = DRIVER_ROOT_URL;
            } else {
                requestUrl = DRIVER_ITEMS_URL;
            }
            path = StringUtils.removeLastSeparator(path);

            ResponseEntity<String> responseEntity = oneDriveRestTemplate.getForEntity(requestUrl, String.class, getGraphEndPoint(), path);
            String body = responseEntity.getBody();

            JSONObject root = JSON.parseObject(body);

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


    public FileItemDTO getItem(String path) {
        String requestUrl;

        ResponseEntity<String> responseEntity = oneDriveRestTemplate.getForEntity(DRIVER_ITEM_URL, String.class, path);
        String body = responseEntity.getBody();

        JSONObject fileItem = JSON.parseObject(body);

        FileItemDTO fileItemDTO = new FileItemDTO();
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
        return fileItemDTO;
    }


    public abstract String getGraphEndPoint();

    public abstract String getAuthenticateEndPoint();

    public abstract String getClientId();

    public abstract String getRedirectUri();

    public abstract String getClientSecret();

    public abstract String getScope();
}
