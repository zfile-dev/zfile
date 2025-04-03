package im.zhaojun.zfile.module.user.model.response;

import im.zhaojun.zfile.module.user.model.dto.UserStorageSourceDetailDTO;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserDetailResponse {

    private Integer id;

    private String username;

    private String nickname;

    private Set<String> defaultPermissions;

    private List<UserStorageSourceDetailDTO> userStorageSourceList;

    private Boolean enable;

    private Date createTime;

}
