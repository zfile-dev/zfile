package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Google Drive 初始化参数
 *
 * @author zhaojun
 */
@Getter
@ToString
public class GoogleDriveParam extends ProxyTransferParam {
	
	@StorageParamItem(name = "clientId", defaultValue = "${zfile.gd.clientId}", order = 1, description = "默认 API 仅用作示例，因审核原因，目前不可用，请自行申请 API", link = "https://docs.zfile.vip/advanced#google-drive-api", linkName = "自定义 API 文档")
	private String clientId;
	
	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.gd.clientSecret}", order = 2)
	private String clientSecret;
	
	@StorageParamItem(name = "回调地址", description = "这里要修改为自己的域名", defaultValue = "${zfile.gd.redirectUri}", order = 3)
	private String redirectUri;
	
	@Setter
	@StorageParamItem(name = "访问令牌", link = "/gd/authorize", linkName = "前往获取令牌", order = 4)
	private String accessToken;
	
	@Setter
	@StorageParamItem(name = "刷新令牌", order = 5)
	private String refreshToken;
	
	@StorageParamItem(name = "网盘", order = 6, required = false)
	private String driveId;
	
	@StorageParamItem(name = "基路径", defaultValue = "/", order = 7, description = "基路径表示读取的根文件夹，不填写表示允许读取所有。如： '/'，'/文件夹1'")
	private String basePath;
	
	@StorageParamItem(name = "加速域名", required = false, description = "可使用 cf worker index 程序的链接，会使用 cf 中转下载，教程自行查询. 不填写则使用服务器中转下载.")
	private String domain;

}