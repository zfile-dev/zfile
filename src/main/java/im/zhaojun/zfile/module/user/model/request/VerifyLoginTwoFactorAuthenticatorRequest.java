package im.zhaojun.zfile.module.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 验证 2FA 认证返回结果
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@Schema(description = "验证二步验证结果")
public class VerifyLoginTwoFactorAuthenticatorRequest {

	@Schema(title = "二步验证二维码", requiredMode = Schema.RequiredMode.REQUIRED, example = "EwBoxxxxxxxxxxxxxxxbAI=")
	@NotBlank(message = "二步验证密钥不能为空")
	private String secret;

	@Schema(title = "APP 生成的二步验证验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "125612")
	@NotBlank(message = "二步验证验证码不能为空")
	private String code;

}