package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Sharepoint 站点信息
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "SharePoint 站点结果类")
public class SharepointSiteResult {

	@ApiModelProperty(value="站点 id")
	private String id;

	@ApiModelProperty(value="站点名称")
	private String displayName;

	@ApiModelProperty(value="站点地址")
	private String webUrl;

}