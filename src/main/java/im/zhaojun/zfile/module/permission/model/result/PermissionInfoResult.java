package im.zhaojun.zfile.module.permission.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PermissionInfoResult {

    @Schema(name="权限名称")
    private String name;

    @Schema(name="权限标识")
    private String value;

    @Schema(name="权限描述")
    private String tips;

}
