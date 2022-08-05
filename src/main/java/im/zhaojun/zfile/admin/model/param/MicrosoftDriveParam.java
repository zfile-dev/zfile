package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annotation.StorageParamItem;
import lombok.Getter;

/**
 * 微软云初始化参数
 *
 * @author zhaojun
 */
@Getter
public class MicrosoftDriveParam implements IStorageParam {

	@StorageParamItem(name = "clientId", defaultValue = "${zfile.onedrive.clientId}", order = 1,
			description = "可自行更改，但修改后，下方获取访问令牌的地址不可用，需自行获取访问令牌和刷新令牌.")
	private String clientId;

	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.onedrive.clientSecret}", order = 2)
	private String clientSecret;

	@StorageParamItem(name = "访问令牌", link = "/onedrive/authorize", linkName = "前往获取令牌", order = 3)
	private String accessToken;

	@StorageParamItem(name = "刷新令牌", order = 4)
	private String refreshToken;
	
	@StorageParamItem(name = "反代域名", required = false, order = 7, description = "世纪互联版不建议启用，国际版启用后不一定比启用前快，这个要根据仔细网络情况决定.",
			link = "https://docs.zfile.vip/#/advanced?id=onedrive-cf", linkName = "配置文档")
	private String proxyDomain;

	@StorageParamItem(name = "基路径", defaultValue = "/", order = 8, description = "基路径表示读取的根文件夹，不填写表示允许读取所有。如： '/'，'/文件夹1'")
	private String basePath;

}