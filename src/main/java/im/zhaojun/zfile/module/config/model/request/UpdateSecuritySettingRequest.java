package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.login.model.enums.LoginVerifyModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登陆安全设置请求参数类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "登陆安全设置请求参数类")
public class UpdateSecuritySettingRequest {

	@ApiModelProperty(value = "是否在前台显示登陆按钮", example = "true")
	private Boolean showLogin;

	@ApiModelProperty(value = "登陆验证方式，支持验证码和 2FA 认证")
	private LoginVerifyModeEnum loginVerifyMode;

	@ApiModelProperty(value = "登陆验证 Secret")
	private String loginVerifySecret;

}