package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annoation.StorageParamItem;

/**
 * SharePoint 世纪互联初始化参数
 *
 * @author zhaojun
 */
public class SharePointChinaParam extends SharePointParam {

	@StorageParamItem(name = "clientId", defaultValue = "${zfile.onedrive-china.clientId}", order = 1,
			description = "可自行更改，但修改后，则下方获取访问令牌的地址不可用，需自行获取访问令牌和刷新令牌.")
	private String clientId;

	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.onedrive-china.clientSecret}", order = 2)
	private String clientSecret;

	@StorageParamItem(name = "访问令牌", link = "/onedrive/china-authorize", linkName = "前往获取令牌", order = 3)
	private String accessToken;

}