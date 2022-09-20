package im.zhaojun.zfile.module.storage.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * SharePoint 信息请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "SharePoint 信息请求类")
public class SharePointInfoRequest {

    @ApiModelProperty(value = "SharePoint 类型", notes = "Standard(国际版、个人版等) 或 China(世纪互联)", required = true, example = "Standard")
    private String type;

    @ApiModelProperty(value = "访问令牌 (accessToken)", required = true, example = "EwBoxxxxxxxxxxxxxxxbAI=")
    private String accessToken;

    @ApiModelProperty(value = "域名前缀", required = true, example = "zfile")
    private String domainPrefix;

    @ApiModelProperty(value = "站点类型", required = true, example = "/sites/")
    private String siteType;

    @ApiModelProperty(value = "站点名称", required = true, example = "zfile")
    private String siteName;

    @ApiModelProperty(value = "域名类型", notes = "com 或 cn", example = "com")
    private String domainType;

}