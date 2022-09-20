package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.annotation.impl.EncodingStorageParamSelect;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;

/**
 * 本地存储初始化参数
 *
 * @author zhaojun
 */
@Getter
public class FtpParam extends ProxyDownloadParam {

	@StorageParamItem(name = "域名或 IP")
	private String host;

	@StorageParamItem(name = "端口")
	private int port;

	@StorageParamItem(name = "编码格式",
			defaultValue = "UTF-8",
			type = StorageParamTypeEnum.SELECT,
			optionsClass = EncodingStorageParamSelect.class,
			description = "表示文件夹及文件名称的编码格式，不表示文本内容的编码格式.")
	private String encoding;

	@StorageParamItem(name = "用户名", required = false)
	private String username;

	@StorageParamItem(name = "密码", required = false)
	private String password;

	@StorageParamItem(name = "加速域名", required = false, description = "如不配置加速域名，则使用服务器中转下载, 反之则使用加速域名下载.")
	private String domain;

	@StorageParamItem(name = "基路径", defaultValue = "/", description = "基路径表示读取的根文件夹，不填写表示允许读取所有。如： '/'，'/文件夹1'")
	private String basePath;

}