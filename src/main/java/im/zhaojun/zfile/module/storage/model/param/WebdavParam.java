package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;

/**
 * WebDav 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class WebdavParam extends ProxyTransferParam {

	@StorageParamItem(key = "url", name = "WebDAV地址", order = 1)
	private String url;

	@StorageParamItem(key = "username", name = "用户名", required = false, order = 2)
	private String username;

	@StorageParamItem(key = "password", name = "密码", required = false, order = 3)
	private String password;

	@StorageParamItem(name = "基路径", required = false, defaultValue = "/", description = "基路径表示该存储源哪个目录在 ZFile 中作为根目录，如： '/'，'/文件夹1'", order = 4)
	private String basePath;

	@StorageParamItem(key = "redirectMode", name = "重定向模式", condition = "domain?==", type = StorageParamTypeEnum.SWITCH, required = false, defaultValue = "false", description = "启用后下载会直接重定向到 Webdav 原地址，即<font style=\"font-weight: bold\">WebDAV地址/文件路径/文件名</font>，而不是中转下载.（此功能需 WebDAV 服务端支持匿名下载，因为中转下载时会携带认证信息）", order = 5)
	private boolean redirectMode;

	@StorageParamItem(condition = "redirectMode?==false", description = "类似于重定向模式，只不过使用的不是上面配置的 WebDAV 地址，而是该字段的地址(请确认你配置的这个地址支持拼接路径后匿名下载).", onlyOverwrite = { StorageParamItemAnnoEnum.CONDITION, StorageParamItemAnnoEnum.DESCRIPTION })
	private String domain;

	@StorageParamItem(condition = "domain==&&redirectMode==false", onlyOverwrite = StorageParamItemAnnoEnum.CONDITION)
	private boolean proxyPrivate;

}