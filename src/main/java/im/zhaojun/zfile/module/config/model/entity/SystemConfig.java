package im.zhaojun.zfile.module.config.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统设置 entity
 *
 * @author zhaojun
 */
@Data
@Schema(description = "系统设置")
@TableName(value = "system_config")
public class SystemConfig implements Serializable {

    public static final String DIRECT_LINK_PREFIX_NAME = "directLinkPrefix";

    public static final String SECURE_LOGIN_ENTRY_NAME = "secureLoginEntry";

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "name")
    @Schema(title = "系统设置名称", example = "siteName")
    private String name;


    @TableField(value = "`value`")
    @Schema(title = "系统设置值", example = "ZFile 演示站")
    private String value;


    @TableField(value = "title")
    @Schema(title = "系统设置描述", example = "站点名称")
    private String title;

}