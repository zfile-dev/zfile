package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import lombok.Getter;

/**
 * MinIO 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class MinIOParam extends S3BaseParam {

	@StorageParamItem(name = "服务地址", order = 30, description = "为 minio 的服务地址，非 web 访问地址，需包含协议，如 http://ip:9000")
	private String endPoint;

	@StorageParamItem(ignoreInput = true, onlyOverwrite = { StorageParamItemAnnoEnum.IGNORE_INPUT })
	private String endPointScheme;

	@StorageParamItem(name = "地域", defaultValue = "minio", order = 45)
	private String region;

	@StorageParamItem(description = "为 minio 的服务地址，非 web 访问地址，一般为 http://ip:9000", order = 65, onlyOverwrite = { StorageParamItemAnnoEnum.DESCRIPTION, StorageParamItemAnnoEnum.ORDER })
	private String domain;

}