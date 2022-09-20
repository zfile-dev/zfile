package im.zhaojun.zfile.module.config.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
@ApiModel(description = "WebDAV 设置请求参数类")
public class UpdateWebDAVRequest {
	
	@ApiModelProperty(value = "启用 WebDAV", example = "true")
	private Boolean webdavEnable;
	
	@ApiModelProperty(value = "WebDAV 服务器中转下载", example = "true")
	private Boolean webdavProxy;
	
	@ApiModelProperty(value = "WebDAV 账号", example = "admin")
	private String webdavUsername;
	
	@ApiModelProperty(value = "WebDAV 密码", example = "123456")
	private String webdavPassword;
	
}