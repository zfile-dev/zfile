package im.zhaojun.zfile.module.config.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户修改密码请求参数类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "用户修改密码请求参数类")
public class UpdateUserNameAndPasswordRequest {

    @Schema(title = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(title = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

}