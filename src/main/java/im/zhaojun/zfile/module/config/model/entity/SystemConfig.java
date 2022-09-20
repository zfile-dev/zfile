package im.zhaojun.zfile.module.config.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统设置 entity
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "系统设置")
@TableName(value = "system_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "name")
    @ApiModelProperty(value = "系统设置名称", example = "siteName")
    private String name;


    @TableField(value = "`value`")
    @ApiModelProperty(value = "系统设置值", example = "ZFile 演示站")
    private String value;


    @TableField(value = "title")
    @ApiModelProperty(value = "系统设置描述", example = "站点名称")
    private String title;

}