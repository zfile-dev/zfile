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
@Schema(name="全局站点设置响应类")
public class FrontSiteConfigResult {

	@Schema(name = "是否已初始化", example = "true")
	private Boolean installed;

	@Schema(name = "Debug 模式", example = "true", description ="开启 debug 模式后，可重置管理员密码")
	private Boolean debugMode;

	@Schema(name = "直链地址前缀", example = "true", description ="直链地址前缀, 如 http(s)://ip:port/${直链前缀}/path/filename")
	private String directLinkPrefix;

	@Schema(name = "站点名称", example = "ZFile Site Name")
	private String siteName;

	@Schema(name = "备案号", example = "冀ICP备12345678号-1")
	private String icp;

	@Schema(name = "页面布局", description ="full:全屏,center:居中", example = "full", requiredMode = Schema.RequiredMode.REQUIRED)
	private String layout;

	@Schema(name = "移动端页面布局", description ="full:全屏,center:居中", example = "full")
	private String mobileLayout;

	@Schema(name = "列表尺寸", description ="large:大,default:中,small:小", example = "default", requiredMode = Schema.RequiredMode.REQUIRED)
	private String tableSize;

	@Schema(name = "是否显示生成直链功能（含直链和路径短链）", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showLinkBtn;

	@Schema(name = "是否显示生成短链功能", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showShortLink;

	@Schema(name = "是否显示生成路径链接功能", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showPathLink;

	@Schema(name = "是否显示文档区", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showDocument;

	@Schema(name = "是否显示网站公告", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showAnnouncement;

	@Schema(name = "网站公告", example = "ZFile 网站公告")
	private String announcement;

	@Schema(name = "自定义 JS")
	private String customJs;

	@Schema(name = "自定义 CSS")
	private String customCss;

	@Schema(name = "自定义视频文件后缀格式")
	private String customVideoSuffix;

	@Schema(name = "自定义图像文件后缀格式")
	private String customImageSuffix;

	@Schema(name = "自定义音频文件后缀格式")
	private String customAudioSuffix;

	@Schema(name = "自定义文本文件后缀格式")
	private String customTextSuffix;

	@Schema(name = "自定义Office后缀格式")
	private String customOfficeSuffix;

	@Schema(name = "自定义kkFileView后缀格式")
	private String customKkFileViewSuffix;

	@Schema(name = "根目录是否显示所有存储源", description ="勾选则根目录显示所有存储源列表, 反之会自动显示第一个存储源的内容.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean rootShowStorage;

	@Schema(name = "强制后端地址", description ="强制指定生成直链，短链，获取回调地址时的地址。", example = "http://xxx.example.com")
	private String forceBackendAddress;

	@Schema(name = "前端域名", description ="前端域名，前后端分离情况下需要配置.", example = "http://xxx.example.com")
	private String frontDomain;

	@Schema(name = "是否在前台显示登陆按钮", example = "true")
	private Boolean showLogin;

	@Schema(name = "登录日志模式", example = "all")
	private LoginLogModeEnum loginLogMode;

	@Schema(name = "默认文件点击习惯", example = "click")
	private FileClickModeEnum fileClickMode;

	@Schema(name = "移动端默认文件点击习惯", example = "click")
	private FileClickModeEnum mobileFileClickMode;

	@Schema(name = "最大同时上传文件数", example = "5")
	private Integer maxFileUploads;

	@Schema(name = "onlyOffice 在线预览地址", example = "http://office.zfile.vip")
	private String onlyOfficeUrl;

	@Schema(name = "kkFileView 在线预览地址", example = "http://kkfile.zfile.vip")
	private String kkFileViewUrl;

	@Schema(name = "默认最大显示文件数", example = "1000")
	private Integer maxShowSize;

	@Schema(name = "每次加载更多文件数", example = "50")
	private Integer loadMoreSize;

	@Schema(name = "默认排序字段", example = "name")
	private String defaultSortField;

	@Schema(name = "默认排序方向", example = "asc")
	private String defaultSortOrder;

	@Schema(name = "站点 Home 名称", example = "xxx 的小站")
	private String siteHomeName;

	@Schema(name = "站点 Home Logo", example = "true")
	private String siteHomeLogo;

	@Schema(name = "站点 Logo 点击后链接", example = "https://www.zfile.vip")
	private String siteHomeLogoLink;

	@Schema(name = "站点 Logo 链接打开方式", example = "_blank")
	private String siteHomeLogoTargetMode;

	@Schema(name = "短链过期时间设置", example = "[{value: 1, unit: \"day\"}, {value: 1, unit: \"week\"}, {value: 1, unit: \"month\"}, {value: 1, unit: \"year\"}]")
	private List<LinkExpireDTO> linkExpireTimes;;

	@Schema(name = "是否默认记住密码", example = "true")
	private Boolean defaultSavePwd;

	/**
	 * 废弃的字段，不再使用悬浮菜单
	 */
	@Deprecated
	@Schema(name = "是否启用 hover 菜单", example = "true")
	private Boolean enableHoverMenu;

	@Schema(name = "是否是游客", example = "true")
	private boolean guest;

}