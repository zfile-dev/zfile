package im.zhaojun.zfile.model.entity;

import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * 驱动器
 *
 * @author zhaojun
 */
@Entity(name = "DRIVER_CONFIG")
@Data
public class DriveConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Boolean enableCache;

    private Boolean autoRefreshCache;

    private StorageTypeEnum type;

    private Boolean searchEnable;

    private Boolean searchIgnoreCase;

    private Boolean searchContainEncryptedFile;

}