package im.zhaojun.zfile.module.config.model.result;

import im.zhaojun.zfile.module.config.model.dto.LinkExpireDTO;
import im.zhaojun.zfile.module.config.model.enums.FileClickModeEnum;
import im.zhaojun.zfile.module.user.model.enums.LoginLogModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 全局站点设置响应类
 *
 * @author zhaojun
 */
@Data
@Schema(title="全局站点设置响应类")
public class FrontSiteConfigResult {

	@Schema(title = "是否已初始化", example = "true")
	private Boolean installed;

	@Schema(title = "Debug 模式", example = "true", description ="开启 debug 模式后，可重置管理员密码")
	private Boolean debugMode;

	@Schema(title = "直链地址前缀", example = "true", description ="直链地址前缀, 如 http(s)://ip:port/${直链前缀}/path/filename")
	private String directLinkPrefix;

	@Schema(title = "站点名称", example = "ZFile Site Name")
	private String siteName;

	@Schema(title = "备案号", example = "冀ICP备12345678号-1")
	private String icp;

	@Schema(title = "页面布局", description ="full:全屏,center:居中", example = "full", requiredMode = Schema.RequiredMode.REQUIRED)
	private String layout;

	@Schema(title = "移动端页面布局", description ="full:全屏,center:居中", example = "full")
	private String mobileLayout;

	@Schema(title = "移动端显示文件大小", description = "仅适用列表视图", example = "true")
	private Boolean mobileShowSize;

	@Schema(title = "列表尺寸", description ="large:大,default:中,small:小", example = "default", requiredMode = Schema.RequiredMode.REQUIRED)
	private String tableSize;

	@Schema(title = "是否显示生成直链功能（含直链和路径短链）", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showLinkBtn;

	@Schema(title = "是否显示生成短链功能", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showShortLink;

	@Schema(title = "是否显示生成路径链接功能", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showPathLink;

	@Schema(title = "是否显示文档区", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showDocument;

	@Schema(title = "是否显示网站公告", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showAnnouncement;

	@Schema(title = "网站公告", example = "ZFile 网站公告")
	private String announcement;

	@Schema(title = "自定义 JS")
	private String customJs;

	@Schema(title = "自定义 CSS")
	private String customCss;

	@Schema(title = "自定义视频文件后缀格式")
	private String customVideoSuffix;

	@Schema(title = "自定义图像文件后缀格式")
	private String customImageSuffix;

	@Schema(title = "自定义音频文件后缀格式")
	private String customAudioSuffix;

	@Schema(title = "自定义文本文件后缀格式")
	private String customTextSuffix;

	@Schema(title = "自定义Office后缀格式")
	private String customOfficeSuffix;

	@Schema(title = "自定义kkFileView后缀格式")
	private String customKkFileViewSuffix;

	@Schema(title = "根目录是否显示所有存储源", description ="勾选则根目录显示所有存储源列表, 反之会自动显示第一个存储源的内容.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean rootShowStorage;

	@Schema(title = "强制后端地址", description ="强制指定生成直链，短链，获取回调地址时的地址。", example = "http://xxx.example.com")
	private String forceBackendAddress;

	@Schema(title = "前端域名", description ="前端域名，前后端分离情况下需要配置.", example = "http://xxx.example.com")
	private String frontDomain;

	@Schema(title = "是否在前台显示登陆按钮", example = "true")
	private Boolean showLogin;

	@Schema(title = "登录日志模式", example = "all")
	private LoginLogModeEnum loginLogMode;

	@Schema(title = "默认文件点击习惯", example = "click")
	private FileClickModeEnum fileClickMode;

	@Schema(title = "移动端默认文件点击习惯", example = "click")
	private FileClickModeEnum mobileFileClickMode;

	@Schema(title = "最大同时上传文件数", example = "5")
	private Integer maxFileUploads;

	@Schema(title = "onlyOffice 在线预览地址", example = "http://office.zfile.vip")
	private String onlyOfficeUrl;

	@Schema(title = "kkFileView 在线预览地址", example = "http://kkfile.zfile.vip")
	private String kkFileViewUrl;

	@Schema(title = "kkFileView 预览方式", example = "iframe/newTab")
	private String kkFileViewOpenMode;

	@Schema(title = "默认最大显示文件数", example = "1000")
	private Integer maxShowSize;

	@Schema(title = "每次加载更多文件数", example = "50")
	private Integer loadMoreSize;

	@Schema(title = "默认排序字段", example = "name")
	private String defaultSortField;

	@Schema(title = "默认排序方向", example = "asc")
	private String defaultSortOrder;

	@Schema(title = "站点 Home 名称", example = "xxx 的小站")
	private String siteHomeName;

	@Schema(title = "站点 Home Logo", example = "true")
	private String siteHomeLogo;

	@Schema(title = "站点 Logo 点击后链接", example = "https://www.zfile.vip")
	private String siteHomeLogoLink;

	@Schema(title = "站点 Logo 链接打开方式", example = "_blank")
	private String siteHomeLogoTargetMode;

	@Schema(title = "短链过期时间设置", example = "[{value: 1, unit: \"day\"}, {value: 1, unit: \"week\"}, {value: 1, unit: \"month\"}, {value: 1, unit: \"year\"}]")
	private List<LinkExpireDTO> linkExpireTimes;

	@Schema(title = "是否默认记住密码", example = "true")
	private Boolean defaultSavePwd;

	@Schema(title = "普通下载是否启用确认弹窗", example = "true")
	private Boolean enableNormalDownloadConfirm;

	@Schema(title = "打包下载是否启用确认弹窗", example = "true")
	private Boolean enablePackageDownloadConfirm;

	@Schema(title = "批量下载是否启用确认弹窗", example = "true")
	private Boolean enableBatchDownloadConfirm;

	/**
	 * 废弃的字段，不再使用悬浮菜单
	 */
	@Deprecated
	@Schema(title = "是否启用 hover 菜单", example = "true")
	private Boolean enableHoverMenu;

	@Schema(title = "是否是游客", example = "true")
	private boolean guest;

}