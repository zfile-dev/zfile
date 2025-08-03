package im.zhaojun.zfile.module.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 复制用户名请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "复制用户名请求类")
public class CopyUserRequest {

    @Schema(title = "存储源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "存储源 id 不能为空")
    private Integer fromId;

    @Schema(title = "复制后用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "复制后用户名不能为空")
    private String toUsername;

    @Schema(title = "复制后用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "复制后用户昵称不能为空")
    private String toNickname;

    @Schema(title = "复制后用户密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String toPassword;

}