package im.zhaojun.zfile.module.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储源类型枚举
 *
 * @author zhaojun
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StorageTypeEnum implements IEnum {

    /**
     * 当前系统支持的所有存储源类型
     */
    LOCAL("local", "本地存储"),
    ALIYUN("aliyun", "阿里云 OSS"),
    WEBDAV("webdav", "WebDAV"),
    TENCENT("tencent", "腾讯云 COS"),
    UPYUN("upyun", "又拍云 USS"),
    FTP("ftp", "FTP"),
    SFTP("sftp", "SFTP"),
    HUAWEI("huawei", "华为云 OBS"),
    MINIO("minio", "MINIO"),
    S3("s3", "S3通用协议"),
    ONE_DRIVE("onedrive", "OneDrive"),
    ONE_DRIVE_CHINA("onedrive-china", "OneDrive 世纪互联"),
    SHAREPOINT_DRIVE("sharepoint", "SharePoint"),
    SHAREPOINT_DRIVE_CHINA("sharepoint-china", "SharePoint 世纪互联"),
    GOOGLE_DRIVE("google-drive", "Google Drive"),
    QINIU("qiniu", "七牛云 KODO");

    private static final Map<String, StorageTypeEnum> ENUM_MAP = new HashMap<>();

    static {
        for (StorageTypeEnum type : StorageTypeEnum.values()) {
            ENUM_MAP.put(type.getKey(), type);
        }
    }

    @ApiModelProperty(value = "存储源类型枚举 Key", example = "aliyun")
    @EnumValue
    private final String key;

    @ApiModelProperty(value = "存储源类型枚举描述", example = "阿里云 OSS")
    private final String description;

    StorageTypeEnum(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public String getValue() {
        return key;
    }
}