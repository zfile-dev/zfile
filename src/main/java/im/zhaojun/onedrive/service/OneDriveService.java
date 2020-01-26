package im.zhaojun.onedrive.service;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaojun
 * @date 2020/1/12 12:17
 */
@Service
public class OneDriveService {

    private static final String DRIVER_INFO_URL = "https://graph.microsoft.com/v1.0/drive";

    private static final String DRIVER_ROOT_URL = "https://graph.microsoft.com/v1.0/drive/root/children";

    private static final String DRIVER_ITEMS_URL = "https://graph.microsoft.com/v1.0/drive/root:{path}:/children";

    private static final String AUTHENTICATE_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";

    @Value("${zfile.onedirve.clientId}")
    private String clientId;

    @Value("${zfile.onedirve.redirectUri}")
    private String redirectUri;

    @Value("${zfile.onedirve.clientSecret}")
    private String clientSecret;

    @Value("${zfile.onedirve.scope}")
    private String scope;

    @Resource
    private RestTemplate oneDriveRestTemplate;

    @Resource
    private StorageConfigRepository storageConfigRepository;

    public OneDriveToken getToken(String code) {
        String param = "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&scope=" + scope +
                "&grant_type=authorization_code";

        HttpRequest post = HttpUtil.createPost(AUTHENTICATE_URL);

        post.body(param, "application/x-www-form-urlencoded");
        HttpResponse response = post.execute();
        return JSONObject.parseObject(response.body(), OneDriveToken.class);
    }

    public OneDriveToken getRefreshToken() {
        StorageConfig refreshStorageConfig =
                storageConfigRepository.findByTypeAndKey(StorageTypeEnum.ONE_DRIVE, StorageConfigConstant.REFRESH_TOKEN_KEY);

        String param = "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&client_secret=" + clientSecret +
                "&refresh_token=" + refreshStorageConfig.getValue() +
                "&grant_type=refresh_token";

        HttpRequest post = HttpUtil.createPost(AUTHENTICATE_URL);

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

            ResponseEntity<String> responseEntity = oneDriveRestTemplate.getForEntity(requestUrl, String.class, path);
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

}
