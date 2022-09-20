package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.link.model.enums.RefererTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 直链设置请求参数类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "直链设置请求参数类")
public class UpdateLinkSettingRequest {

	@ApiModelProperty(value = "是否记录下载日志", example = "true")
	private Boolean recordDownloadLog;

	@ApiModelProperty(value = "直链 Referer 防盗链类型")
	private RefererTypeEnum refererType;

	@ApiModelProperty(value = "直链 Referer 是否允许为空")
	private Boolean refererAllowEmpty;

	@ApiModelProperty(value = "直链 Referer 值")
	private String refererValue;

	@ApiModelProperty(value = "直链地址前缀")
	private String directLinkPrefix;

	@ApiModelProperty(value = "是否显示生成直链功能（含直链和路径短链）", example = "true", required = true)
	private Boolean showLinkBtn;

	@ApiModelProperty(value = "是否显示生成短链功能", example = "true", required = true)
	private Boolean showShortLink;

	@ApiModelProperty(value = "是否显示生成路径链接功能", example = "true", required = true)
	private Boolean showPathLink;
	
	@ApiModelProperty(value = "是否允许路径直链可直接访问", example = "true", required = true)
	private Boolean allowPathLinkAnonAccess;

}