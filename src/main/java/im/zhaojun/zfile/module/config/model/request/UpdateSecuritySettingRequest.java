package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.user.model.enums.LoginLogModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登陆安全设置请求参数类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "登陆安全设置请求参数类")
public class UpdateSecuritySettingRequest {

	@Schema(title = "是否在前台显示登陆按钮", example = "true")
	private Boolean showLogin;

    @Schema(title = "安全登录入口", description = "仅允许字母、数字、短横线和下划线，长度不超过 32", example = "admin")
    @Size(max = 32, message = "安全登录入口长度不能超过 32 个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "安全登录入口只能包含字母、数字、短横线和下划线")
    private String secureLoginEntry;

	@Schema(title = "登录日志模式", example = "all")
	private LoginLogModeEnum loginLogMode;

	@Schema(title = "是否启用登陆验证码", example = "true")
	private Boolean loginImgVerify;

	@Schema(title = "是否为管理员启用双因素认证", example = "true")
	private Boolean adminTwoFactorVerify;

	@Schema(title = "2FA登陆验证 Secret")
	private String loginVerifySecret;

	@Schema(title = "匿名用户首页显示内容")
	private String guestIndexHtml;

}