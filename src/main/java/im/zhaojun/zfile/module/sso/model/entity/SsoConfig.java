package im.zhaojun.zfile.module.sso.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单点登录厂商配置
 *
 * @author OnEvent
 */
@Data
@Schema(name = "单点登录厂商配置")
@TableName(value = "`sso_config`")
public class SsoConfig implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "provider", type = IdType.INPUT)
    @Schema(name = "OIDC/OAuth2 厂商名", example = "Logto")
    private String provider;

    @TableField(value = "`issuer`")
    @Schema(name = "SSO 厂商提供的签发人")
    private String issuer;

    @TableField(value = "`client_id`")
    @Schema(name = "在 SSO 厂商处生成的 ID")
    private String clientId;

    @TableField(value = "`client_secret`")
    @Schema(name = "在 SSO 厂商处生成的密钥")
    private String clientSecret;

    @TableField(value = "`auth_url`")
    @Schema(name = "SSO 厂商提供的授权端点")
    private String authUrl;

    @TableField(value = "`token_url`")
    @Schema(name = "SSO 厂商提供的 Token 端点")
    private String tokenUrl;

    @TableField(value = "`user_info_url`")
    @Schema(name = "SSO 厂商提供的用户信息端点")
    private String userInfoUrl;

    @TableField(value = "`scope`")
    @Schema(name = "SSO 厂商提供的授权范围")
    private String scope;

    @TableField(value = "`well_known_url`")
    @Schema(name = "SSO 厂商提供的发现端点，填写此项则会忽略其他的各项 URL 配置")
    private String wellKnownUrl;

    @TableField(value = "`binding_field`")
    @Schema(name = "SSO 系统中用户与本系统中用户互相的绑定字段")
    private String bindingField;

    @TableField(value = "`enabled`")
    @Schema(name = "是否启用")
    private Boolean enabled;
}
