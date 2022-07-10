package im.zhaojun.zfile.admin.model.result.login;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登陆 2FA 认证生成返回结果
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@ApiModel(description = "生成二步验证结果")
public class Login2FAResult {

	@ApiModelProperty(value = "二步验证二维码")
	private String qrcode;

	@ApiModelProperty(value = "二步验证密钥")
	private String secret;

}