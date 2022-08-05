package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annotation.StorageParamItem;
import lombok.Getter;

/**
 * OneDrive 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class OneDriveChinaParam extends OneDriveParam {

	@StorageParamItem(name = "clientId", defaultValue = "${zfile.onedrive-china.clientId}",
			description = "可自行更改，但修改后，则下方获取访问令牌的地址不可用，需自行获取访问令牌和刷新令牌.", order = 1)
	private String clientId;

	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.onedrive-china.clientSecret}", order = 2)
	private String clientSecret;

	@StorageParamItem(name = "访问令牌", link = "/onedrive/china-authorize", linkName = "前往获取令牌", order = 3)
	private String accessToken;

}