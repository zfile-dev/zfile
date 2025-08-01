package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhaojun
 */
@Getter
@Setter
public class DogeCloudParam extends S3BaseParam {

    @StorageParamItem(ignoreInput = true, onlyOverwrite = { StorageParamItemAnnoEnum.IGNORE_INPUT })
    private String endPoint;

    @StorageParamItem(ignoreInput = true, onlyOverwrite = { StorageParamItemAnnoEnum.IGNORE_INPUT })
    private String endPointScheme;

    @StorageParamItem(ignoreInput = true, onlyOverwrite = { StorageParamItemAnnoEnum.IGNORE_INPUT })
    private String bucketName;

    @StorageParamItem(name = "存储空间名称", order = 40)
    private String originBucketName;

    @StorageParamItem(ignoreInput = true, onlyOverwrite = { StorageParamItemAnnoEnum.IGNORE_INPUT })
    private boolean isPrivate;

    @StorageParamItem(ignoreInput = true, onlyOverwrite = { StorageParamItemAnnoEnum.IGNORE_INPUT })
    private Integer tokenTime;

}