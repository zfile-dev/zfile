package im.zhaojun.zfile.module.password.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 密码设置 entity
 *
 * @author zhaojun
 */
@Data
@Schema(title="密码设置")
@TableName(value = "password_config")
public class PasswordConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "storage_id")
    @Schema(title = "存储源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer storageId;


    @TableField(value = "expression")
    @Schema(title = "密码文件夹表达式", requiredMode = Schema.RequiredMode.REQUIRED, example = "/*.png")
    private String expression;


    @TableField(value = "password")
    @Schema(title = "密码值", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;


    @TableField(value = "description")
    @Schema(title = "表达式描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "用来辅助记忆表达式")
    private String description;

}