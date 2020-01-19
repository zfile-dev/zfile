package im.zhaojun.onedrive.service;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author zhaojun
 * @date 2020/1/18 17:28
 */
@Data
public class OneDriveToken {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "refresh_token")
    private String refreshToken;
}
