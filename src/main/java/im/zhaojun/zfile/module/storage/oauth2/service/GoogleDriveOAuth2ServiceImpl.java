package im.zhaojun.zfile.module.storage.oauth2.service;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.dto.OAuth2TokenDTO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;

@Slf4j
@Component
public class GoogleDriveOAuth2ServiceImpl implements IOAuth2Service {

    @Resource
    private ZFileProperties zFileProperties;

    public static final String GOOGLE_OAUTH2_URL = "https://accounts.google.com/o/oauth2/token";

    @Override
    public String generateAuthorizationUrl(String clientId, String clientSecret, String redirectUri) {
        if (StringUtils.isAllEmpty(clientId, clientSecret, redirectUri)) {
            clientId = zFileProperties.getGd().getClientId();
            clientSecret = zFileProperties.getGd().getClientSecret();
            redirectUri = zFileProperties.getGd().getRedirectUri();
        }

        String stateStr = "&state=" + Base64.encodeUrlSafe(StringUtils.join("::", clientId, clientSecret, redirectUri));

        return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + clientId
                + "&response_type=code&redirect_uri=" + redirectUri
                + "&scope=" + zFileProperties.getGd().getScope()
                + "&access_type=offline"
                + "&prompt=consent"
                + stateStr;
    }

    @Override
    public OAuth2TokenDTO getTokenByCode(String code, String clientId, String clientSecret, String redirectUri) {
        if (StringUtils.isAllEmpty(clientId, clientSecret, redirectUri)) {
            clientId = zFileProperties.getGd().getClientId();
            clientSecret = zFileProperties.getGd().getClientSecret();
            redirectUri = zFileProperties.getGd().getRedirectUri();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String clientCredentials = Base64.encodeUrlSafe(clientId + ":" + clientSecret);
        headers.add("Authorization", "Basic " + clientCredentials);
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("code", code);
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("scope", zFileProperties.getGd().getScope());

        HttpEntity<MultiValueMap<String, String>> formEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = new RestTemplate(new NoRedirectClientHttpRequestFactory())
                                                .exchange(GOOGLE_OAUTH2_URL, HttpMethod.POST, formEntity, String.class);

        String responseBody = response.getBody();
        if (response.getStatusCode() != HttpStatus.OK) {
            return OAuth2TokenDTO.fail(clientId, clientSecret, redirectUri, responseBody);
        }

        JSONObject jsonBody = JSONObject.parseObject(responseBody);

        String accessToken = jsonBody.getString(ACCESS_TOKEN_FIELD_NAME);
        String refreshToken = jsonBody.getString(REFRESH_TOKEN_FIELD_NAME);
        Integer expiresIn = jsonBody.getInteger(EXPIRES_IN_FIELD_NAME);
        return OAuth2TokenDTO.success(clientId, clientSecret, redirectUri, accessToken, refreshToken, responseBody, expiresIn);
    }


    private static class NoRedirectClientHttpRequestFactory extends
            SimpleClientHttpRequestFactory {

        @Override
        protected void prepareConnection(@NotNull HttpURLConnection connection,
                                         @NotNull String httpMethod) throws IOException {
            super.prepareConnection(connection, httpMethod);
            connection.setInstanceFollowRedirects(true);
        }
    }

}
