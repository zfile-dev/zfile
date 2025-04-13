package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import lombok.Getter;

/**
 * 又拍云初始化参数
 *
 * @author zhaojun
 */
@Getter
public class UpYunParam extends OptionalProxyTransferParam {

	@StorageParamItem(name = "存储空间名称", order = 1)
	private String bucketName;

	@StorageParamItem(name = "操作员名称", order = 2)
	private String username;

	@StorageParamItem(name = "操作员密码", order = 3)
	private String password;

	@StorageParamItem(name = "下载域名", description = "填写您在又拍云绑定的域名.", required = false, order = 4)
	private String domain;

	@StorageParamItem(name = "基路径", defaultValue = "/", description = "基路径表示该存储源哪个目录在 ZFile 中作为根目录，如： '/'，'/文件夹1'", order = 5)
	private String basePath;

	@StorageParamItem(name = "Token", required = false,
			condition = "enableProxyDownload==false",
			link = "https://help.upyun.com/knowledge-base/cdn-token-limite/", linkName = "官方配置文档",
			description = "可在又拍云后台开启 \"访问控制\" -> \"Token 防盗链\"，控制资源内容的访问时限，即时间戳防盗链。", order = 6)
	private String token;

}