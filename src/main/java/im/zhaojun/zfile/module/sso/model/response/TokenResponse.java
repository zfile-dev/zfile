package im.zhaojun.zfile.module.sso.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenResponse
{
    private String accessToken;
    private String idToken;
    private String scope;
    private String tokenType;
    private Long expiresIn;
}
