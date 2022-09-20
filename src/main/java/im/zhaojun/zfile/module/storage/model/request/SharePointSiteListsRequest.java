package im.zhaojun.zfile.module.storage.model.request;

import im.zhaojun.zfile.core.validation.StringListValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取 SharePoint 网站下目录请求
 *
 * @author zhaojun
 */
@Data
public class SharePointSiteListsRequest {

	@StringListValue(message = "账号类型只能是 Standard（标准版、国际版）或 China（世纪互联）", vals = {"Standard", "China"})
	private String type;

	@ApiModelProperty(value = "访问令牌 (accessToken)", required = true, example = "EwBoxxxxxxxxxxxxxxxbAI=")
	@NotBlank(message = "访问令牌不能为空")
	private String accessToken;

	@ApiModelProperty(value = "站点 ID (siteId)", required = true, example = "a046ac3a-ea74-13c5-8b8f-233599507d96 或 xxx.sharepoint.cn,a046ac3a-ea74-13c5-8b8f-233599507d96,ec7e71ed-9065-4190-b471-b91c28c30bb1  如果是后者，则会自动截取中间那部分，结果和前者相同")
	@NotBlank(message = "siteId 不能为空")
	private String siteId;

}