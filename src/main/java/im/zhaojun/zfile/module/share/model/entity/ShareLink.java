package im.zhaojun.zfile.module.share.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.module.share.model.enums.ShareTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(title="分享文件(夹)")
@TableName(value = "share_link")
public class ShareLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "分享ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(title = "分享链接 key")
    @TableField(value = "share_key")
    private String shareKey;

    @Schema(title = "分享密码")
    @TableField(value = "`password`")
    private String password;

    @Schema(title = "过期时间")
    @TableField(value = "expire_date")
    private Date expireDate;

    @Schema(title = "存储源 key")
    @TableField(value = "storage_key")
    private String storageKey;

    @Schema(title = "分享所在目录")
    @TableField(value = "share_path")
    private String sharePath;

    @Schema(title = "分享项目(JSON格式存储文件或文件夹名称)")
    @TableField(value = "share_item")
    private String shareItem;

    @Schema(title = "创建时间")
    @TableField(value = "create_date")
    private Date createDate;

    @Schema(title = "分享类型", description = "FILE/FOLDER/MULTIPLE")
    @TableField(value = "share_type")
    private ShareTypeEnum shareType;

    @Schema(title = "创建分享的用户ID")
    @TableField(value = "user_id")
    private Integer userId;

    @Schema(title = "下载次数")
    @TableField(value = "download_count")
    private Integer downloadCount;

    @Schema(title = "访问次数")
    @TableField(value = "access_count")
    private Integer accessCount;

    @Schema(title = "是否已过期")
    @TableField(exist = false)
    private Boolean expired;
}