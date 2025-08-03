package im.zhaojun.zfile.module.config.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 站点访问控制请求参数类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "站点访问控制参数类")
public class UpdateAccessSettingRequest {

    @Schema(title = "访问 ip 黑名单", example = "162.13.1.0/24\n192.168.1.1")
    private String accessIpBlocklist;

    @Schema(title = "访问 ua 黑名单", example = "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36*")
    private String accessUaBlocklist;

}
