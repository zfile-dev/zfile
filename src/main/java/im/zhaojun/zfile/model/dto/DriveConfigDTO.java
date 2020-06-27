package im.zhaojun.zfile.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.model.enums.StorageTypeEnumJsonDeSerializerConvert;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class DriveConfigDTO {

    private Integer id;

    private String name;

    @JsonDeserialize(using = StorageTypeEnumJsonDeSerializerConvert.class)
    private StorageTypeEnum type;

    private Boolean enable;

    private boolean enableCache;

    private boolean autoRefreshCache;

    private boolean searchEnable;

    private boolean searchIgnoreCase;

    private boolean searchContainEncryptedFile;

    private Integer orderNum;

    private StorageStrategyConfig storageStrategyConfig;

}