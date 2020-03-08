package im.zhaojun.zfile.model.support;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class OneDriveToken {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "refresh_token")
    private String refreshToken;
}
