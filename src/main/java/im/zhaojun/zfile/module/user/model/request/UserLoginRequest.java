package im.zhaojun.zfile.module.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求参数参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "用户登录请求参数类")
public class UserLoginRequest {

    @Schema(title = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(title = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(title = "验证码", example = "123456")
    private String verifyCode;

    @Schema(title = "验证码 UUID", description ="用于图形验证码确认每个验证码图片请求的唯一值.", example = "c140a792-4ca2-4dac-8d4c-35750b78524f")
    private String verifyCodeUUID;

}