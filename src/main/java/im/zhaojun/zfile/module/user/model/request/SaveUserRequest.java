package im.zhaojun.zfile.module.user.model.request;

import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SaveUserRequest {

    @Schema(name="用户 id")
    private Integer id;

    @Schema(name="用户名")
    private String username;

    @Schema(name="昵称")
    private String nickname;

    @Schema(name="密码")
    private String password;

    @Schema(name="盐")
    private String salt;

    @Schema(name="用户默认权限", description ="当新增存储源时, 自动授予该用户新存储源的权限.")
    private Set<String> defaultPermissions;

    @Schema(name="授予给用户的存储策略列表")
    private List<UserStorageSource> userStorageSourceList;

    @Schema(name="是否启用")
    private Boolean enable;

}