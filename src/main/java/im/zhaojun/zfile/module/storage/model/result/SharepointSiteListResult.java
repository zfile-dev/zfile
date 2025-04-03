package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * Sharepoint 网站 list 列表
 *
 * @author zhaojun
 */
@Data
@Schema(description = "Sharepoint 网站 list 列表")
public class SharepointSiteListResult {

	@Schema(name="站点目录 id")
	private String id;

	@Schema(name="站点目录名称")
	private String displayName;

	@Schema(name="站点目录创建时间")
	private Date createdDateTime;

	@Schema(name="站点目录地址")
	private String webUrl;

}