package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import lombok.Getter;

/**
 * 微软云初始化参数
 *
 * @author zhaojun
 */
@Getter
public class MicrosoftDriveParam extends OptionalProxyTransferParam {

	@StorageParamItem(name = "clientId", defaultValue = "${zfile.onedrive.clientId}", order = 1)
	private String clientId;

	@StorageParamItem(name = "SecretKey", defaultValue = "${zfile.onedrive.clientSecret}", order = 2)
	private String clientSecret;
	
	@StorageParamItem(name = "回调地址", description = "如使用自定义 api, 需将此处默认的域名修改为您的域名, 且需在 api 中配置为回调域名.", defaultValue = "${zfile.onedrive.redirectUri}", order = 3)
	private String redirectUri;

	@StorageParamItem(name = "访问令牌", link = "/onedrive/authorize", linkName = "前往获取令牌", order = 4)
	private String accessToken;

	@StorageParamItem(name = "刷新令牌", order = 5)
	private String refreshToken;

	@StorageParamItem(name = "刷新令牌到期时间戳(秒)", hidden = true, required = false)
	private Integer refreshTokenExpiredAt;

	@StorageParamItem(name = "基路径", defaultValue = "/", order = 6, description = "基路径表示该存储源哪个目录在 ZFile 中作为根目录，如： '/'，'/文件夹1'")
	private String basePath;

	@StorageParamItem(name = "加速域名", required = false, order = 7, description = "使用 CF 或自建反代下载地址时填写，否则请留空。")
	private String proxyDomain;

	@StorageParamItem(name = "加速域名", ignoreInput = true)
	private String domain;

	@StorageParamItem(name = "代理上传超时时间", condition = "enableProxyUpload==true", defaultValue = "300", description = "服务器代理上传至微软云的超时时间, 单位为秒, 默认为 300 秒. 请自行根据服务器带宽大小、上传文件调整，为 0 则不限制.", order = 101)
	private Integer proxyUploadTimeoutSecond;

	@StorageParamItem(condition = "proxyDomain==", onlyOverwrite = {StorageParamItemAnnoEnum.CONDITION, StorageParamItemAnnoEnum.ORDER}, order = 101)
	private boolean enableProxyDownload;

}