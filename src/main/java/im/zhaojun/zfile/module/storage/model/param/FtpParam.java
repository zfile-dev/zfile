package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.module.storage.annotation.impl.EncodingStorageParamSelect;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import im.zhaojun.zfile.module.storage.service.impl.FtpServiceImpl;
import lombok.Getter;

/**
 * 本地存储初始化参数
 *
 * @author zhaojun
 */
@Getter
public class FtpParam extends ProxyTransferParam {

	@StorageParamItem(name = "域名或 IP", order = 1)
	private String host;

	@StorageParamItem(name = "端口", order = 2)
	private int port;

	@StorageParamItem(name = "编码格式",
			defaultValue = "UTF-8",
			type = StorageParamTypeEnum.SELECT,
			optionsClass = EncodingStorageParamSelect.class,
			description = "表示文件夹及文件名称的编码格式，不表示文本内容的编码格式.", order = 3)
	private String encoding;

	@StorageParamItem(name = "用户名", required = false, order = 4, description = "如果是匿名访问，不填写保存失败的话，可能用户名需要写 anonymous")
	private String username;

	@StorageParamItem(name = "密码", required = false, order = 5)
	private String password;

	@StorageParamItem(name = "基路径", defaultValue = "/", description = "基路径表示该存储源哪个目录在 ZFile 中作为根目录，如： '/'，'/文件夹1'", order = 6)
	private String basePath;

	@StorageParamItem(order = 7, onlyOverwrite = { StorageParamItemAnnoEnum.ORDER })
	private String domain;

	@StorageParamItem(name = "FTP 模式",
				condition = "domain==",
				type = StorageParamTypeEnum.SELECT,
				options = {
					@StorageParamSelectOption(value = FtpServiceImpl.FTP_MODE_ACTIVE, label = "主动模式"),
					@StorageParamSelectOption(value = FtpServiceImpl.FTP_MODE_PASSIVE, label = "被动模式")
				},
				defaultValue = "passive",
				description = "主动模式为 FTP 服务端主动连接客户端(随机开放端口，需保证防火墙无限制端口)，被动模式为 FTP 服务端被动等待客户端连接.",
			order = 8)
	private String ftpMode;

	@StorageParamItem(name = "支持 Range", condition = "domain==", type = StorageParamTypeEnum.SWITCH, defaultValue = "false", description = "启用后会支持多线程下载、断点续传、下载显示进度，但会对 FTP 服务端带来更大压力", order = 9)
	private boolean enableRange;

}