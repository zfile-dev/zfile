package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Sharepoint 网站 list 列表
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "Sharepoint 网站 list 列表")
public class SharepointSiteListResult {

	@ApiModelProperty(value="站点目录 id")
	private String id;

	@ApiModelProperty(value="站点目录名称")
	private String displayName;

	@ApiModelProperty(value="站点目录创建时间")
	private Date createdDateTime;

	@ApiModelProperty(value="站点目录地址")
	private String webUrl;

}