package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.config.model.enums.FileClickModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 显示设置请求参数类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "显示设置请求参数类")
public class UpdateViewSettingRequest {

	@Schema(title = "根目录是否显示所有存储源", description ="勾选则根目录显示所有存储源列表, 反之会自动显示第一个存储源的内容.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean rootShowStorage;

	@Schema(title = "页面布局", description ="full:全屏,center:居中", example = "full", requiredMode = Schema.RequiredMode.REQUIRED)
	private String layout;

	@Schema(title = "移动端页面布局", description ="full:全屏,center:居中", example = "full")
	private String mobileLayout;

	@Schema(title = "移动端显示文件大小", description = "仅适用列表视图", example = "true")
	private Boolean mobileShowSize;

	@Schema(title = "列表尺寸", description ="large:大,default:中,small:小", example = "default", requiredMode = Schema.RequiredMode.REQUIRED)
	private String tableSize;

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

	@Schema(title = "是否显示文档区", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showDocument;

	@Schema(title = "是否显示网站公告", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showAnnouncement;

	@Schema(title = "网站公告", example = "ZFile 网站公告")
	private String announcement;

	@Schema(title = "自定义 CSS")
	private String customCss;

	@Schema(title = "自定义 JS")
	private String customJs;

	@Schema(title = "默认文件点击习惯", example = "click")
	private FileClickModeEnum fileClickMode;

	@Schema(title = "移动端默认文件点击习惯", example = "click")
	private FileClickModeEnum mobileFileClickMode;

	@Schema(title = "onlyOffice 在线预览地址", example = "http://office.zfile.vip")
	private String onlyOfficeUrl;

	@Schema(title = "onlyOffice Secret", example = "X9rBGypwWE86Lca8e4Mo55iHFoiyh9ed")
	private String onlyOfficeSecret;

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

}