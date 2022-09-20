package im.zhaojun.zfile.module.config.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统初始化请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "系统初始化请求类")
public class InstallSystemRequest {

    @ApiModelProperty(value = "站点名称", example = "ZFile Site Name")
    private String siteName;

    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;

    @ApiModelProperty(value = "密码", example = "123456")
    private String password;

    @ApiModelProperty(value = "站点域名", example = "https://zfile.vip")
    private String domain;

}