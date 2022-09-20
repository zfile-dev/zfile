package im.zhaojun.zfile.module.readme.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.zfile.module.readme.model.enums.ReadmeDisplayModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * readme 文档配置 entity
 *
 * @author zhaojun
 */
@Data
@ApiModel(value="readme 文档配置")
@TableName(value = "`readme_config`")
public class ReadmeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value = "ID, 新增无需填写", example = "1")
    @JsonIgnore
    private Integer id;


    @TableField(value = "`storage_id`")
    @ApiModelProperty(value="存储源 ID")
    private Integer storageId;


    @TableField(value = "`description`")
    @ApiModelProperty(value = "表达式描述", required = true, example = "用来辅助记忆表达式")
    private String description;


    @TableField(value = "`expression`")
    @ApiModelProperty(value="路径表达式")
    private String expression;


    @TableField(value = "`readme_text`")
    @ApiModelProperty(value="readme 文本内容, 支持 md 语法.")
    private String readmeText;


    @TableField(value = "`display_mode`")
    @ApiModelProperty(value = "显示模式", required = true, example = "readme 显示模式，支持顶部显示: top, 底部显示:bottom, 弹窗显示: dialog")
    private ReadmeDisplayModeEnum displayMode;

}