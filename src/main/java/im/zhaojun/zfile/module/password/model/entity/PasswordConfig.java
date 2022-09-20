package im.zhaojun.zfile.module.password.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 密码设置 entity
 *
 * @author zhaojun
 */
@Data
@ApiModel(value="密码设置")
@TableName(value = "password_config")
public class PasswordConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "ID, 新增无需填写", example = "1")
    @JsonIgnore
    private Integer id;


    @TableField(value = "storage_id")
    @ApiModelProperty(value = "存储源 ID", required = true, example = "1")
    private Integer storageId;


    @TableField(value = "expression")
    @ApiModelProperty(value = "密码文件夹表达式", required = true, example = "/*.png")
    private String expression;


    @TableField(value = "password")
    @ApiModelProperty(value = "密码值", required = true, example = "123456")
    private String password;


    @TableField(value = "description")
    @ApiModelProperty(value = "表达式描述", required = true, example = "用来辅助记忆表达式")
    private String description;

}