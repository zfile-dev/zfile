package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhaojun
 */
@Getter
@Setter
public class DogeCloudParam extends S3BaseParam {

    @StorageParamItem(name = "区域", ignoreInput = true, description = "如下拉列表中没有的区域，或想使用内网地址，可直接输入后回车，如: xxx-cn-beijing.example.com")
    private String endPoint;

    @StorageParamItem(name = "存储空间名称", ignoreInput = true)
    private String bucketName;

    @StorageParamItem(name = "S3AccessKey", ignoreInput = true)
    private String s3AccessKey;

    @StorageParamItem(name = "S3SecretKey", ignoreInput = true)
    private String s3SecretKey;

    @StorageParamItem(name = "S3SessionToken", ignoreInput = true)
    private String s3SessionToken;

    @StorageParamItem(name = "存储空间名称", order = 4)
    private String originBucketName;

    @StorageParamItem(name = "是否是私有空间", type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "私有空间会生成带签名的下载链接", ignoreInput = true)
    private boolean isPrivate;

    @StorageParamItem(name = "下载签名有效期", required = false, defaultValue = "1800", description = "当为私有空间时, 用于下载签名的有效期, 单位为秒, 如不配置则默认为 1800 秒.", ignoreInput = true)
    private Integer tokenTime;

    @StorageParamItem(name = "是否自动配置 CORS 跨域设置", order = 100, type = StorageParamTypeEnum.SWITCH, defaultValue = "true",
            description = "如不配置跨域设置，可能会无法导致无法上传，或上传后看不到文件（某些 S3 存储无需配置此选项，如 Cloudflare R2、Oracle 对象存储）", ignoreInput = true)
    private boolean autoConfigCors;

}