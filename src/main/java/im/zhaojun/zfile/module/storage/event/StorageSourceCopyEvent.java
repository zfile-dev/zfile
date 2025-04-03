package im.zhaojun.zfile.module.storage.event;

import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class StorageSourceCopyEvent {

    private Integer fromId;

    private Integer newId;

    public StorageSourceCopyEvent(Integer fromId, Integer newId) {
        this.fromId = fromId;
        this.newId = newId;
    }

}