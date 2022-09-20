package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import lombok.Getter;

/**
 * 腾讯云初始化参数
 *
 * @author zhaojun
 */
@Getter
public class TencentParam extends S3BaseParam {

	@StorageParamItem(key = "secretId", name = "SecretId", order = 1)
	private String accessKey;

}