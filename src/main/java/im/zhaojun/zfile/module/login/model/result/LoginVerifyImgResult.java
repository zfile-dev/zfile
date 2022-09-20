package im.zhaojun.zfile.module.login.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 生成图片验证码结果类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "生成图片验证码结果类")
public class LoginVerifyImgResult {

	@ApiModelProperty(value = "验证码图片", example = "data:image/png;base64,iajsiAAA...")
	private String imgBase64;

	@ApiModelProperty(value = "验证码 UUID", example = "c140a792-4ca2-4dac-8d4c-35750b78524f")
	private String uuid;

}