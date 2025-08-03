package im.zhaojun.zfile.module.user.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 生成图片验证码结果类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "生成图片验证码结果类")
public class LoginVerifyImgResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(title = "验证码图片", example = "data:image/png;base64,iajsiAAA...")
	private String imgBase64;

	@Schema(title = "验证码 UUID", example = "c140a792-4ca2-4dac-8d4c-35750b78524f")
	private String uuid;

}