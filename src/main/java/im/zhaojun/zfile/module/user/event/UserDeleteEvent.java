package im.zhaojun.zfile.module.user.event;

import im.zhaojun.zfile.module.user.model.entity.User;
import lombok.Data;

/**
 * 复制用户事件
 *
 * @author zhaojun
 */
@Data
public class UserDeleteEvent {

    private Integer id;

    private String username;

    public UserDeleteEvent(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }

}