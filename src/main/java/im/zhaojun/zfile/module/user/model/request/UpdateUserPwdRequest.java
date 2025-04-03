package im.zhaojun.zfile.module.user.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateUserPwdRequest {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

}