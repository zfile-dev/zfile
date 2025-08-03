package im.zhaojun.zfile.module.user.model.request;

import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SaveUserRequest {

    @Schema(title="用户 id")
    private Integer id;

    @Schema(title="用户名")
    private String username;

    @Schema(title="昵称")
    private String nickname;

    @Schema(title="密码")
    private String password;

    @Schema(title="盐")
    private String salt;

    @Schema(title="用户默认权限", description ="当新增存储源时, 自动授予该用户新存储源的权限.")
    private Set<String> defaultPermissions;

    @Schema(title="授予给用户的存储策略列表")
    private List<UserStorageSource> userStorageSourceList;

    @Schema(title="是否启用")
    private Boolean enable;

}