package im.zhaojun.zfile.module.sso.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenResponse
{
    private String idToken;
    private String accessToken;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private Long expiresIn;
    private String refreshTokenExpiresIn;
}
