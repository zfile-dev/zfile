package im.zhaojun.zfile.module.readme.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.zfile.module.readme.model.enums.ReadmeDisplayModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * readme 文档配置 entity
 *
 * @author zhaojun
 */
@Data
@Schema(name="readme 文档配置")
@TableName(value = "`readme_config`")
public class ReadmeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @Schema(name = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "`storage_id`")
    @Schema(name="存储源 ID")
    private Integer storageId;


    @TableField(value = "`description`")
    @Schema(name = "表达式描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "用来辅助记忆表达式")
    private String description;


    @TableField(value = "`expression`")
    @Schema(name="路径表达式")
    private String expression;


    @TableField(value = "`readme_text`")
    @Schema(name="readme 文本内容, 支持 md 语法.")
    private String readmeText;


    @TableField(value = "`display_mode`")
    @Schema(name = "显示模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "readme 显示模式，支持顶部显示: top, 底部显示:bottom, 弹窗显示: dialog")
    private ReadmeDisplayModeEnum displayMode;

}