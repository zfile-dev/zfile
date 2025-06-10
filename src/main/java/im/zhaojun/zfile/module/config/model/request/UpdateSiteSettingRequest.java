package im.zhaojun.zfile.module.config.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 站点设置请求参数类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "站点设置请求参数类")
public class UpdateSiteSettingRequest {

	@Schema(name = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZFile Site Name")
	@NotBlank(message = "站点名称不能为空")
	private String siteName;

	@Schema(name = "强制后端地址", description ="强制指定生成直链，短链，获取回调地址时的地址。", example = "http://xxx.example.com")
	private String forceBackendAddress;

	@Schema(name = "前端域名", description ="前端域名，前后端分离情况下需要配置.", example = "http://xxx.example.com")
	private String frontDomain;

	@Schema(name = "头像地址", example = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png")
	private String avatar;

	@Schema(name = "备案号", example = "冀ICP备12345678号-1")
	private String icp;

	@Schema(name = "授权码", example = "e619510f-cdcd-f657-6c5e-2d12e9a28ae5")
	private String authCode;

	@Schema(name = "最大同时上传文件数", example = "5")
	private Integer maxFileUploads;

	@Schema(name = "站点 Home 名称", example = "xxx 的小站")
	private String siteHomeName;

	@Schema(name = "站点 Home Logo", example = "true")
	private String siteHomeLogo;

	@Schema(name = "站点 Logo 点击后链接", example = "https://www.zfile.vip")
	private String siteHomeLogoLink;

	@Schema(name = "站点 Logo 链接打开方式", example = "_blank")
	private String siteHomeLogoTargetMode;

	@Schema(name = "网站 favicon 图标地址", example = "https://www.example.com/favicon.ico")
	private String faviconUrl;

	@Schema(name = "管理员页面点击 Logo 回到首页打开方式", example = "_blank")
	private String siteAdminLogoTargetMode;

	@Schema(name = "管理员页面点击版本号打开更新日志", example = "true")
	private Boolean siteAdminVersionOpenChangeLog;

}