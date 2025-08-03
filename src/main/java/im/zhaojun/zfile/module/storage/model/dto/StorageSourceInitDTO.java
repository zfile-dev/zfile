package im.zhaojun.zfile.module.storage.model.dto;

import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class StorageSourceInitDTO {

    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;

    @Schema(title = "存储源名称", example = "阿里云 OSS 存储")
    private String name;

    @Schema(title = "存储源别名", example = "存储源别名，用于 URL 中展示, 如 http://ip:port/{存储源别名}")
    private String key;

    @Schema(title = "存储源类型", example = "ftp")
    private StorageTypeEnum type;

    @Schema(title = "存储源参数")
    List<StorageSourceConfig> storageSourceConfigList;

    public static StorageSourceInitDTO convert(StorageSource storageSource, List<StorageSourceConfig> storageSourceConfigList) {
        StorageSourceInitDTO storageSourceInitDTO = new StorageSourceInitDTO();
        storageSourceInitDTO.setId(storageSource.getId());
        storageSourceInitDTO.setType(storageSource.getType());
        storageSourceInitDTO.setName(storageSource.getName());
        storageSourceInitDTO.setKey(storageSource.getKey());
        storageSourceInitDTO.setStorageSourceConfigList(storageSourceConfigList);
        return storageSourceInitDTO;
    }

}
