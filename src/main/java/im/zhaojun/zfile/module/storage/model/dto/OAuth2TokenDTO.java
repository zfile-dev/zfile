package im.zhaojun.zfile.module.storage.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * OneDrive Token DTO
 *
 * @author zhaojun
 */
@ToString
@Data
public class OAuth2TokenDTO {
    
    private String clientId;
    
    private String clientSecret;
    
    private String redirectUri;
    
    private String accessToken;
    
    private String refreshToken;
    
    private boolean success;
    
    private String body;

    /**
     * 令牌到期时间，时间戳，单位毫秒
     */
    private Integer expiredAt;
    
    public static OAuth2TokenDTO success(String clientId, String clientSecret, String redirectUri, String accessToken, String refreshToken, String body, Integer expiredAt) {
        OAuth2TokenDTO token = new OAuth2TokenDTO();
        token.setClientId(clientId);
        token.setClientSecret(clientSecret);
        token.setRedirectUri(redirectUri);
        token.setSuccess(true);
        token.setBody(body);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiredAt(expiredAt);
        return token;
    }
    
    public static OAuth2TokenDTO fail(String clientId, String clientSecret, String redirectUri, String body) {
        OAuth2TokenDTO token = new OAuth2TokenDTO();
        token.setClientId(clientId);
        token.setClientSecret(clientSecret);
        token.setRedirectUri(redirectUri);
        token.setSuccess(false);
        token.setBody(body);
        return token;
    }
    
}