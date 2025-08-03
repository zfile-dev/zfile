package im.zhaojun.zfile.module.storage.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * SharePoint 信息请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "SharePoint 信息请求类")
public class SharePointInfoRequest {

    @Schema(title = "SharePoint 类型", description ="Standard(国际版、个人版等) 或 China(世纪互联)", required = true, example = "Standard")
    private String type;

    @Schema(title = "访问令牌 (accessToken)", required = true, example = "EwBoxxxxxxxxxxxxxxxbAI=")
    private String accessToken;

    @Schema(title = "域名前缀", requiredMode = Schema.RequiredMode.REQUIRED, example = "zfile")
    private String domainPrefix;

    @Schema(title = "站点类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "/sites/")
    private String siteType;

    @Schema(title = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "zfile")
    private String siteName;

    @Schema(title = "域名类型", description ="com 或 cn", example = "com")
    private String domainType;

}