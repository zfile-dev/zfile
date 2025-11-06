package im.zhaojun.zfile.module.share.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 验证分享密码请求
 *
 * @author zhaojun
 */
@Data
@Schema(description = "验证分享密码请求")
public class VerifySharePasswordRequest {

    @Schema(title = "分享链接 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc12345")
    @NotBlank(message = "分享链接 key 不能为空")
    private String shareKey;

    @Schema(title = "分享密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "分享密码不能为空")
    private String password;
}