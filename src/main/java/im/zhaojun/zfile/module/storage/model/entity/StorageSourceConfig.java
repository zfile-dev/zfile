package im.zhaojun.zfile.module.storage.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 存储源拓展属性 entity
 *
 * @author zhaojun
 */
@Data
@Schema(description = "存储源拓展属性")
@TableName(value = "storage_source_config")
public class StorageSourceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "`name`")
    @Schema(title = "存储源属性名称 name", example = "bucketName")
    private String name;


    @TableField(value = "`type`")
    @Schema(title = "存储源类型")
    private StorageTypeEnum type;


    @TableField(value = "title")
    @Schema(title = "存储源属性名称", example = "Bucket 名称")
    private String title;


    @TableField(value = "storage_id")
    @Schema(title = "存储源 id", example = "1")
    private Integer storageId;


    @TableField(value = "`value`")
    @Schema(title = "存储源对应的值", example = "my-bucket")
    private String value;

}