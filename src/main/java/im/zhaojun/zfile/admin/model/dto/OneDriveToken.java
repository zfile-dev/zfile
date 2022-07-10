package im.zhaojun.zfile.admin.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * OneDrive Token DTO
 *
 * @author zhaojun
 */
@Data
public class OneDriveToken {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "refresh_token")
    private String refreshToken;

}