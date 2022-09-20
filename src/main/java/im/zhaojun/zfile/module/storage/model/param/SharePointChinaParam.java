package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import lombok.Getter;

/**
 * SharePoint 世纪互联初始化参数
 *
 * @author zhaojun
 */
@Getter
public class SharePointChinaParam extends SharePointParam {

	@StorageParamItem(name = "clientId", defaultValue = "${zfile.onedrive-china.clientId}", order = 1)
	private String clientId;

	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.onedrive-china.clientSecret}", order = 2)
	private String clientSecret;
	
	@StorageParamItem(name = "回调地址", description = "如使用自定义 api, 需将此处默认的域名修改为您的域名, 且需在 api 中配置为回调域名.",
			defaultValue = "${zfile.onedrive-china.redirectUri}", order = 3)
	private String redirectUri;

	@StorageParamItem(name = "访问令牌", link = "/onedrive/china-authorize", linkName = "前往获取令牌", order = 3)
	private String accessToken;

}