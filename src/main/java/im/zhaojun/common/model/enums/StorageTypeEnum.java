package im.zhaojun.common.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhaojun
 */
public enum StorageTypeEnum {

    /**
     * 当前系统支持的所有存储策略
     */
    UPYUN("upyun", "又拍云 USS"),
    QINIU("qiniu", "七牛云 KODO"),
    HUAWEI("huawei", "华为云 OBS"),
    ALIYUN("aliyun", "阿里云 OSS"),
    FTP("ftp", "FTP"),
    LOCAL("local", "本地存储"),
    TENCENT("tencent", "腾讯云 COS"),
    MINIO("minio", "MINIO");

    private String key;
    private String description;

    private static Map<String, StorageTypeEnum> enumMap = new HashMap<>();

    static {
        for (StorageTypeEnum type : StorageTypeEnum.values()) {
            enumMap.put(type.getKey(), type);
        }
    }

    StorageTypeEnum(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static StorageTypeEnum getEnum(String value) {
        return enumMap.get(value);
    }

}
