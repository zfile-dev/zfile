package im.zhaojun.zfile.module.install.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统初始化请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "系统初始化请求类")
public class InstallSystemRequest {

    @Schema(name = "站点名称", example = "ZFile Site Name")
    private String siteName;

    @Schema(name = "用户名", example = "admin")
    private String username;

    @Schema(name = "密码", example = "123456")
    private String password;

}