package im.zhaojun.zfile.module.config.model.request;

import im.zhaojun.zfile.module.config.model.dto.LinkExpireDTO;
import im.zhaojun.zfile.module.link.model.enums.RefererTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 直链设置请求参数类
 *
 * @author zhaojun
 */
@Data
@Schema(description = "直链设置请求参数类")
public class UpdateLinkSettingRequest {

	@Schema(title = "是否记录下载日志", example = "true")
	private Boolean recordDownloadLog;

	@Schema(title = "直链 Referer 防盗链类型")
	private RefererTypeEnum refererType;

	@Schema(title = "直链 Referer 是否允许为空")
	private Boolean refererAllowEmpty;

	@Schema(title = "直链 Referer 值")
	private String refererValue;

	@Schema(title = "直链地址前缀")
	@NotBlank(message = "直链地址前缀不能为空")
	private String directLinkPrefix;

	@Schema(title = "是否显示生成直链功能（含直链和路径短链）", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showLinkBtn;

	@Schema(title = "是否显示生成短链功能", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showShortLink;

	@Schema(title = "是否显示生成路径链接功能", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean showPathLink;

	@Schema(title = "是否允许路径直链可直接访问", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean allowPathLinkAnonAccess;

	@Schema(title = "限制直链下载秒数", example = "_blank")
	private Integer linkLimitSecond;

	@Schema(title = "限制直链下载次数", example = "_blank")
	private Integer linkDownloadLimit;

	@Schema(title = "短链过期时间设置", example = "[{value: 1, unit: \"day\"}, {value: 1, unit: \"week\"}, {value: 1, unit: \"month\"}, {value: 1, unit: \"year\"}]")
	private List<LinkExpireDTO> linkExpireTimes;

}