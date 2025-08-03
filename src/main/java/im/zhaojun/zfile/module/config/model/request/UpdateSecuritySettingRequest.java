package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.user.model.enums.LoginLogModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
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