package im.zhaojun.zfile.module.user.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置用
 *
 * @author zhaojun
 */
@Data
public class ResetAdminUserNameAndPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

}