package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;

/**
 * 代理上传下载参数
 *
 * @author zhaojun
 */
@Getter
public class ProxyTransferParam implements IStorageParam {

	@StorageParamItem(name = "加速域名", required = false, description = "如配置加速域名，则会使用你指定的域名+文件路径生成下载链接，不写则默认使用服务器中转下载(除非你知道你在做什么，不然一般不用填写该值).", order = 10)
	private String domain;

	@StorageParamItem(name = "生成签名链接", condition = "domain==", type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "代理下载会生成带签名的链接, 如不想对外开放直链, 可以防止被当做直链使用（类似于对象存储的私有空间）.", order = 20)
	private boolean proxyPrivate;

	@StorageParamItem(name = "下载签名有效期", condition = "proxyPrivate==true", required = false, defaultValue = "1800", description = "用于下载签名的有效期, 单位为秒, 如不配置则默认为 1800 秒.", order = 30)
	private Integer proxyTokenTime;

	@StorageParamItem(name = "下载链接强制下载", type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "关闭则使用浏览器默认行为，启用则强制下载", order = 50)
	private boolean proxyLinkForceDownload;

}