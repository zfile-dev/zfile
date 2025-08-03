package im.zhaojun.zfile.module.storage.model.request;

import im.zhaojun.zfile.core.validation.StringListValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * SharePoint 搜索网站列表请求
 *
 * @author zhaojun
 */
@Data
public class SharePointSearchSitesRequest {

	@StringListValue(message = "账号类型只能是 Standard（标准版、国际版）或 China（世纪互联）", vals = {"Standard", "China"})
	private String type;

	@Schema(title = "访问令牌 (accessToken)", required = true, example = "EwBoxxxxxxxxxxxxxxxbAI=")
	@NotBlank(message = "访问令牌不能为空")
	private String accessToken;

}