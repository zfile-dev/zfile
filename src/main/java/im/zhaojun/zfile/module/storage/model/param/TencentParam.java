package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
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

	@StorageParamItem(name = "是否是私有空间", order = 7, type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "私有空间会生成带签名的下载链接. <font color=\"red\">如您使用自定义CDN域名，且在腾讯云开启了回源鉴权，请务必关闭此选项。</font>")
	private boolean isPrivate;

}