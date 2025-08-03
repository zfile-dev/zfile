package im.zhaojun.zfile.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.zfile.core.config.mybatis.CollectionStrTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@TableName(value = "`user`", autoResultMap = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "`username`")
    private String username;

    @TableField(value = "`nickname`")
    private String nickname;

    @TableField(value = "`password`")
    @JsonIgnore
    private String password;

    @Schema(title="盐")
    @JsonIgnore
    private String salt;

    @TableField(value = "`enable`")
    private Boolean enable;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    @TableField(value = "default_permissions", typeHandler = CollectionStrTypeHandler.class)
    private Set<String> defaultPermissions;

}