package im.zhaojun.zfile.module.link.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短链信息 entity
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "短链信息")
@TableName(value = "short_link")
public class ShortLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "storage_id")
    @ApiModelProperty(value = "存储源 ID", example = "1")
    private Integer storageId;


    @TableField(value = "short_key")
    @ApiModelProperty(value = "短链 key", example = "voldd3")
    private String shortKey;


    @TableField(value = "url")
    @ApiModelProperty(value = "短链 url", example = "/directlink/1/test02.png")
    private String url;


    @TableField(value = "create_date")
    @ApiModelProperty(value = "创建时间", example = "2021-11-22 10:05")
    private Date createDate;

}