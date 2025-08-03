package im.zhaojun.zfile.module.sso.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单点登录厂商配置
 *
 * @author OnEvent
 */
@Data
@Schema(title = "单点登录厂商配置")
@TableName(value = "`sso_config`")
public class SsoConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "provider")
    @Schema(title = "OIDC/OAuth2 厂商名", example = "Logto", description = "简称，仅可包含数字、字母，-，_")
    @NotBlank(message = "OIDC/OAuth2 厂商名不能为空")
    private String provider;

    @TableField(value = "`name`")
    @Schema(title = "显示名称", description = "登录页悬浮到图标上的名称")
    @NotBlank(message = "显示名称不能为空")
    private String name;

    @TableField(value = "`icon`")
    @Schema(title = "ICON", description = "登录页显示的图标，支持 URL、SVG、Base64 格式")
    @NotBlank(message = "ICON 不能为空")
    private String icon;

    @TableField(value = "`client_id`")
    @Schema(title = "在 SSO 厂商处生成的 ID")
    @NotBlank(message = "client_id 不能为空")
    private String clientId;

    @TableField(value = "`client_secret`")
    @Schema(title = "在 SSO 厂商处生成的密钥")
    @NotBlank(message = "client_secret 不能为空")
    private String clientSecret;

    @TableField(value = "`auth_url`")
    @Schema(title = "SSO 厂商提供的授权端点")
    @NotBlank(message = "auth_url 不能为空")
    private String authUrl;

    @TableField(value = "`token_url`")
    @Schema(title = "SSO 厂商提供的 Token 端点")
    @NotBlank(message = "token_url 不能为空")
    private String tokenUrl;

    @TableField(value = "`user_info_url`")
    @Schema(title = "SSO 厂商提供的用户信息端点")
    @NotBlank(message = "user_info_url 不能为空")
    private String userInfoUrl;

    @TableField(value = "`scope`")
    @Schema(title = "SSO 厂商提供的授权范围")
    @NotBlank(message = "scope 不能为空")
    private String scope;

    @TableField(value = "`binding_field`")
    @Schema(title = "SSO 系统中用户与本系统中用户互相的绑定字段")
    @NotBlank(message = "用户字段表达式不能为空")
    private String bindingField;

    @TableField(value = "`enabled`")
    @Schema(title = "是否启用")
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @TableField(value = "`order_num`")
    @Schema(title = "排序", description = "数字越小越靠前")
    private Integer orderNum;

}