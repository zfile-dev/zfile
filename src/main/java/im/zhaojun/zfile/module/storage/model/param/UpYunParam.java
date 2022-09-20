package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import lombok.Getter;

/**
 * 又拍云初始化参数
 *
 * @author zhaojun
 */
@Getter
public class UpYunParam implements IStorageParam {

	@StorageParamItem(name = "存储空间名称")
	private String bucketName;

	@StorageParamItem(name = "用户名")
	private String username;

	@StorageParamItem(name = "密码")
	private String password;

	@StorageParamItem(name = "下载域名", description = "填写您在又拍云绑定的域名.")
	private String domain;

	@StorageParamItem(name = "基路径", defaultValue = "/", description = "基路径表示读取的根文件夹，不填写表示允许读取所有。如： '/'，'/文件夹1'")
	private String basePath;

	@StorageParamItem(name = "Token", required = false, link = "https://help.upyun.com/knowledge-base/cdn-token-limite/", linkName = "官方配置文档",description = "可在又拍云后台开启 \"访问控制\" -> \"Token 防盗链\"，控制资源内容的访问时限，即时间戳防盗链。")
	private String token;

	@StorageParamItem(name = "Token 有效期", required = false, defaultValue = "1800", description = "Token (防盗链)有效期，单位为秒。")
	private int tokenTime;

}