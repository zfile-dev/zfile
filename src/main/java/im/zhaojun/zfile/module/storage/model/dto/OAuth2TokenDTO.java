package im.zhaojun.zfile.module.storage.model.dto;

import lombok.Data;

/**
 * OneDrive Token DTO
 *
 * @author zhaojun
 */
@Data
public class OAuth2TokenDTO {
    
    private String clientId;
    
    private String clientSecret;
    
    private String redirectUri;
    
    private String accessToken;
    
    private String refreshToken;
    
    private boolean success;
    
    private String body;
    
    public static OAuth2TokenDTO success(String clientId, String clientSecret, String redirectUri, String accessToken, String refreshToken, String body) {
        OAuth2TokenDTO token = new OAuth2TokenDTO();
        token.setClientId(clientId);
        token.setClientSecret(clientSecret);
        token.setRedirectUri(redirectUri);
        token.setSuccess(true);
        token.setBody(body);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
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