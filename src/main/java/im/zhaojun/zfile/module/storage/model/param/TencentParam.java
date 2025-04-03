package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
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

	@StorageParamItem(name = "SecretKey", order = 2)
	private String secretKey;

	@StorageParamItem(description = "如果使用自定义加速域名，请在腾讯云控制台关闭回源鉴权功能，否则同时勾选下面的私有空间时会冲突导致下载失败.", onlyOverwrite = { StorageParamItemAnnoEnum.DESCRIPTION })
	private String domain;

	@StorageParamItem(description = "私有空间会生成带签名的下载链接. <font color=\"red\">如您使用自定义CDN域名，且在腾讯云开启了回源鉴权，请务必关闭此选项。</font>", onlyOverwrite = { StorageParamItemAnnoEnum.DESCRIPTION })
	private boolean isPrivate;

}