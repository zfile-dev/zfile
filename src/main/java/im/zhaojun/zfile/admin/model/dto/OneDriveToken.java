package im.zhaojun.zfile.admin.model.dto;

import lombok.Data;

/**
 * OneDrive Token DTO
 *
 * @author zhaojun
 */
@Data
public class OneDriveToken {
    
    private String clientId;
    
    private String clientSecret;
    
    private String redirectUri;

    private String accessToken;

    private String refreshToken;
    
    private boolean success;
    
    private String body;
    
    public static OneDriveToken success(String clientId, String clientSecret, String redirectUri, String accessToken, String refreshToken, String body) {
        OneDriveToken token = new OneDriveToken();
        token.setClientId(clientId);
        token.setClientSecret(clientSecret);
        token.setRedirectUri(redirectUri);
        token.setSuccess(true);
        token.setBody(body);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        return token;
    }
    
    public static OneDriveToken fail(String clientId, String clientSecret, String redirectUri, String body) {
        OneDriveToken token = new OneDriveToken();
        token.setClientId(clientId);
        token.setClientSecret(clientSecret);
        token.setRedirectUri(redirectUri);
        token.setSuccess(false);
        token.setBody(body);
        return token;
    }

}