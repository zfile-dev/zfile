package im.zhaojun.zfile.module.config.model.result;

import im.zhaojun.zfile.module.config.model.enums.FileClickModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 全局站点设置响应类
 *
 * @author zhaojun
 */
@Data
@ApiModel(value="全局站点设置响应类")
public class SiteConfigResult {

	@ApiModelProperty(value = "是否已初始化", example = "true")
	private Boolean installed;

	@ApiModelProperty(value = "Debug 模式", example = "true", notes = "开启 debug 模式后，可重置管理员密码")
	private Boolean debugMode;

	@ApiModelProperty(value = "直链地址前缀", example = "true", notes = "直链地址前缀, 如 http(s)://ip:port/${直链前缀}/path/filename")
	private String directLinkPrefix;

	@ApiModelProperty(value = "站点名称", example = "ZFile Site Name")
	private String siteName;

	@ApiModelProperty(value = "备案号", example = "冀ICP备12345678号-1")
	private String icp;

	@ApiModelProperty(value = "站点域名(后端)", example = "https://zfile.vip", notes = "该值需配置为后端的站点域名，生成直链等操作需要此参数.")
	private String domain;

	@ApiModelProperty(value = "页面布局", notes = "full:全屏,center:居中", example = "full", required = true)
	private String layout;

	@ApiModelProperty(value = "列表尺寸", notes = "large:大,default:中,small:小", example = "default", required = true)
	private String tableSize;

	@ApiModelProperty(value = "是否显示生成直链功能（含直链和路径短链）", example = "true", required = true)
	private Boolean showLinkBtn;

	@ApiModelProperty(value = "是否显示生成短链功能", example = "true", required = true)
	private Boolean showShortLink;

	@ApiModelProperty(value = "是否显示生成路径链接功能", example = "true", required = true)
	private Boolean showPathLink;
	
	@ApiModelProperty(value = "是否显示文档区", example = "true", required = true)
	private Boolean showDocument;

	@ApiModelProperty(value = "是否显示网站公告", example = "true", required = true)
	private Boolean showAnnouncement;

	@ApiModelProperty(value = "网站公告", example = "ZFile 网站公告")
	private String announcement;

	@ApiModelProperty(value = "自定义 JS")
	private String customJs;

	@ApiModelProperty(value = "自定义 CSS")
	private String customCss;

	@ApiModelProperty(value = "自定义视频文件后缀格式")
	private String customVideoSuffix;

	@ApiModelProperty(value = "自定义图像文件后缀格式")
	private String customImageSuffix;

	@ApiModelProperty(value = "自定义音频文件后缀格式")
	private String customAudioSuffix;

	@ApiModelProperty(value = "自定义文本文件后缀格式")
	private String customTextSuffix;

	@ApiModelProperty(value = "根目录是否显示所有存储源", notes = "根目录是否显示所有存储源, 如果为 true, 则根目录显示所有存储源列表, 如果为 false, 则会自动跳转到第一个存储源.", example = "true", required = true)
	private Boolean rootShowStorage;

	@ApiModelProperty(value = "前端域名", notes = "前端域名，前后端分离情况下需要配置.", example = "http://xxx.example.com")
	private String frontDomain;

	@ApiModelProperty(value = "是否在前台显示登陆按钮", example = "true")
	private Boolean showLogin;

	@ApiModelProperty(value = "默认文件点击习惯", example = "click")
	private FileClickModeEnum fileClickMode;
	
	@ApiModelProperty(value = "最大同时上传文件数", example = "5")
	private Integer maxFileUploads;

	@ApiModelProperty(value = "onlyOffice 在线预览地址", example = "http://office.zfile.vip")
	private String onlyOfficeUrl;

}