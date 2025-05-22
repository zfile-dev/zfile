package im.zhaojun.zfile.module.sso.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SsoLoginItemResponse {

    @Schema(name = "OIDC/OAuth2 厂商名", example = "Logto", description = "简称，仅可包含数字、字母，-，_")
    private String provider;

    @Schema(name = "显示名称", description = "登录页悬浮到图标上的名称")
    private String name;

    @Schema(name = "ICON", description = "登录页显示的图标，支持 URL、SVG、Base64 格式")
    private String icon;

}
