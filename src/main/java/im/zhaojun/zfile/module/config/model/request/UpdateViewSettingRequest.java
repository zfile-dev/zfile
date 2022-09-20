package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.config.model.enums.FileClickModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 显示设置请求参数类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "显示设置请求参数类")
public class UpdateViewSettingRequest {

	@ApiModelProperty(value = "根目录是否显示所有存储源", notes = "根目录是否显示所有存储源, 如果为 true, 则根目录显示所有存储源列表, 如果为 false, 则会自动跳转到第一个存储源.", example = "true", required = true)
	private Boolean rootShowStorage;

	@ApiModelProperty(value = "页面布局", notes = "full:全屏,center:居中", example = "full", required = true)
	private String layout;

	@ApiModelProperty(value = "列表尺寸", notes = "large:大,default:中,small:小", example = "default", required = true)
	private String tableSize;

	@ApiModelProperty(value = "自定义视频文件后缀格式")
	private String customVideoSuffix;

	@ApiModelProperty(value = "自定义图像文件后缀格式")
	private String customImageSuffix;

	@ApiModelProperty(value = "自定义音频文件后缀格式")
	private String customAudioSuffix;

	@ApiModelProperty(value = "自定义文本文件后缀格式")
	private String customTextSuffix;

	@ApiModelProperty(value = "是否显示文档区", example = "true", required = true)
	private Boolean showDocument;

	@ApiModelProperty(value = "是否显示网站公告", example = "true", required = true)
	private Boolean showAnnouncement;

	@ApiModelProperty(value = "网站公告", example = "ZFile 网站公告")
	private String announcement;

	@ApiModelProperty(value = "自定义 CSS")
	private String customCss;

	@ApiModelProperty(value = "自定义 JS")
	private String customJs;

	@ApiModelProperty(value = "默认文件点击习惯", example = "click")
	private FileClickModeEnum fileClickMode;

	@ApiModelProperty(value = "onlyOffice 在线预览地址", example = "http://office.zfile.vip")
	private String onlyOfficeUrl;

}