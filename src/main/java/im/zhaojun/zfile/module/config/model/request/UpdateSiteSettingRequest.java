package im.zhaojun.zfile.module.config.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 站点设置请求参数类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "站点设置请求参数类")
public class UpdateSiteSettingRequest {

	@ApiModelProperty(value = "站点名称", required = true, example = "ZFile Site Name")
	@NotBlank(message = "站点名称不能为空")
	private String siteName;

	@ApiModelProperty(value = "站点域名", required = true, example = "https://zfile.vip")
	@NotBlank(message = "站点域名不能为空")
	private String domain;

	@ApiModelProperty(value = "前端域名", notes = "前端域名，前后端分离情况下需要配置.", example = "http://xxx.example.com")
	private String frontDomain;

	@ApiModelProperty(value = "头像地址", example = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png")
	private String avatar;

	@ApiModelProperty(value = "备案号", example = "冀ICP备12345678号-1")
	private String icp;
	
	@ApiModelProperty(value = "最大同时上传文件数", example = "5")
	private Integer maxFileUploads;

}