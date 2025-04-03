package im.zhaojun.zfile.module.user.event;

import lombok.Data;

/**
 * 复制用户事件
 *
 * @author zhaojun
 */
@Data
public class UserCopyEvent {

    private Integer fromId;

    private Integer newId;

    public UserCopyEvent(Integer fromId, Integer newId) {
        this.fromId = fromId;
        this.newId = newId;
    }

}