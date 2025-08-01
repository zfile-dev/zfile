package im.zhaojun.zfile.module.storage.oauth2.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public abstract class AbstractMicrosoftOAuth2Service implements IOAuth2Service {

    /**
     * 根据 RefreshToken 获取 AccessToken API URI
     */
    private static final String AUTHENTICATE_URL = "https://{authenticateEndPoint}/common/oauth2/v2.0/token";

    @Override
    public String generateAuthorizationUrl(String clientId, String clientSecret, String redirectUri) {
        if (StringUtils.isAllEmpty(clientId, clientSecret, redirectUri)) {
            clientId = getClientId();
            clientSecret = getClientSecret();
            redirectUri = getRedirectUri();
        }

        String stateStr = "&state=" + Base64.encodeUrlSafe(StringUtils.join("::", clientId, clientSecret, redirectUri));

        return "https://" + getEndPoint() + "/common/oauth2/v2.0/authorize?client_id=" + clientId
                + "&response_type=code&redirect_uri=" + redirectUri
                + "&scope=" + getScope()
                + stateStr;
    }

    @Override
    public OAuth2TokenDTO getTokenByCode(String code, String clientId, String clientSecret, String redirectUri) {
        if (StringUtils.isAllEmpty(clientId, clientSecret, redirectUri)) {
            clientId = getClientId();
            clientSecret = getClientSecret();
            redirectUri = getRedirectUri();
        }
        String param = "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&scope=" + getScope() +
                "&grant_type=authorization_code";


        if (log.isDebugEnabled()) {
            log.debug("根据授权回调 code 获取令牌, 请求参数: [{}]", param);
        }

        String authenticateUrl = AUTHENTICATE_URL.replace("{authenticateEndPoint}", getEndPoint());
        HttpResponse response = HttpUtil.createPost(authenticateUrl)
                .body(param, "application/x-www-form-urlencoded")
                .execute();

        String responseBody = response.body();
        int responseStatus = response.getStatus();
        if (responseStatus != HttpStatus.OK.value()) {
            return OAuth2TokenDTO.fail(clientId, clientSecret, redirectUri, responseBody);
        }

        JSONObject jsonBody = JSONObject.parseObject(responseBody);
        String accessToken = jsonBody.getString(ACCESS_TOKEN_FIELD_NAME);
        String refreshToken = jsonBody.getString(REFRESH_TOKEN_FIELD_NAME);
        Integer expiresIn = jsonBody.getInteger(EXPIRES_IN_FIELD_NAME);
        return OAuth2TokenDTO.success(clientId, clientSecret, redirectUri, accessToken, refreshToken, responseBody, expiresIn);
    }

    public abstract String getEndPoint();

    public abstract String getClientId();
    
    public abstract String getClientSecret();
    
    public abstract String getRedirectUri();
    
    public abstract String getScope();

}
