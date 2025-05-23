package im.zhaojun.zfile.module.sso.model.response;

import lombok.Data;

@Data
public class TokenResponse {

    private String idToken;

    private String accessToken;

    private String refreshToken;

    private String scope;

    private String tokenType;

    private Long expiresIn;

    private String refreshTokenExpiresIn;

}