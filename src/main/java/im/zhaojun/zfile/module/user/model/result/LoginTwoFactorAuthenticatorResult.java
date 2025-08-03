package im.zhaojun.zfile.module.user.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 登陆 2FA 认证生成返回结果
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@Schema(description = "生成二步验证结果")
public class LoginTwoFactorAuthenticatorResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(title = "二步验证二维码")
	private String qrcode;

	@Schema(title = "二步验证密钥")
	private String secret;

}