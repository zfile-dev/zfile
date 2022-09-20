package im.zhaojun.zfile.module.login.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 验证 2FA 认证返回结果
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@ApiModel(description = "验证二步验证结果")
public class VerifyLoginTwoFactorAuthenticatorRequest {

	@ApiModelProperty(value = "二步验证二维码", required = true, example = "EwBoxxxxxxxxxxxxxxxbAI=")
	@NotBlank(message = "二步验证密钥不能为空")
	private String secret;

	@ApiModelProperty(value = "APP 生成的二步验证验证码", required = true, example = "125612")
	@NotBlank(message = "二步验证验证码不能为空")
	private String code;

}