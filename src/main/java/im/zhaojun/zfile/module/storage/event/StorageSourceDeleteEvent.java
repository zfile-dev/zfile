package im.zhaojun.zfile.module.storage.event;

import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class StorageSourceDeleteEvent {

    private Integer id;

    private String key;

    private String name;

    private StorageTypeEnum type;

    public StorageSourceDeleteEvent(StorageSource storageSource) {
        this.id = storageSource.getId();
        this.key = storageSource.getKey();
        this.name = storageSource.getName();
        this.type = storageSource.getType();
    }

}