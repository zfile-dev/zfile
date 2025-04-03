package im.zhaojun.zfile.module.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CheckUserDuplicateRequest {

    @Schema(name="用户 id")
    private Integer id;

    @Schema(name="用户名")
    private String username;

}
