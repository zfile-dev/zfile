package im.zhaojun.zfile.module.storage.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 存储源拓展属性 entity
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "存储源拓展属性")
@TableName(value = "storage_source_config")
public class StorageSourceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "`name`")
    @ApiModelProperty(value = "存储源属性名称 name", example = "bucketName")
    private String name;


    @TableField(value = "`type`")
    @ApiModelProperty(value = "存储源类型")
    private StorageTypeEnum type;


    @TableField(value = "title")
    @ApiModelProperty(value = "存储源属性名称", example = "Bucket 名称")
    private String title;


    @TableField(value = "storage_id")
    @ApiModelProperty(value = "存储源 id", example = "1")
    private Integer storageId;


    @TableField(value = "`value`")
    @ApiModelProperty(value = "存储源对应的值", example = "my-bucket")
    private String value;

}