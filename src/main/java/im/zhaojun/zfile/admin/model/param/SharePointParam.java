package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annotation.StorageParamItem;
import lombok.Getter;

/**
 * SharePoint 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class SharePointParam extends MicrosoftDriveParam {

	@StorageParamItem(name = "clientId", defaultValue = "${zfile.onedrive.clientId}", order = 1,
			description = "可自行更改，但修改后，下方获取访问令牌的地址不可用，需自行获取访问令牌和刷新令牌.")
	private String clientId;

	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.onedrive.clientSecret}", order = 2)
	private String clientSecret;

	@StorageParamItem(name = "网站", order = 5)
	private String siteId;

	@StorageParamItem(name = "子目录", order = 6, description = "表示 SharePoint 子列表/子网站，在世纪互联网站 Tab 卡中 \"网站内容\" 新增.")
	private String listId;

}