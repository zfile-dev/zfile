package im.zhaojun.zfile.module.link.model.entity;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 短链信息 entity
 *
 * @author zhaojun
 */
@Data
@Schema(description = "短链信息")
@TableName(value = "short_link")
public class ShortLink implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 永久直链失效时间为 -1
     */
    public static final Long PERMANENT_EXPIRE_TIME = -1L;

    /**
     * 永久直链失效日期为 9999-12-31
     */
    public static final Date PERMANENT_EXPIRE_DATE = DateUtil.parseDate("9999-12-31");

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(title = "ID, 新增无需填写", example = "1")
    private Integer id;


    @TableField(value = "storage_id")
    @Schema(title = "存储源 ID", example = "1")
    private Integer storageId;


    @TableField(value = "short_key")
    @Schema(title = "短链 key", example = "voldd3")
    private String shortKey;


    @TableField(value = "url")
    @Schema(title = "短链 url", example = "/directlink/1/test02.png")
    private String url;


    @TableField(value = "create_date")
    @Schema(title = "创建时间", example = "2021-11-22 10:05")
    private Date createDate;


    @TableField(value = "expire_date")
    @Schema(title = "过期时间", example = "2021-11-23 10:05")
    private Date expireDate;


}