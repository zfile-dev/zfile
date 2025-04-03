package im.zhaojun.zfile.module.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@TableName(value = "user_storage_source")
public class UserStorageSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "storage_source_id")
    private Integer storageSourceId;

    @TableField(value = "root_path")
    private String rootPath;

    @TableField(value = "`enable`")
    private Boolean enable;

    @TableField(value = "`permissions`", typeHandler = im.zhaojun.zfile.core.config.mybatis.CollectionStrTypeHandler.class)
    private Set<String> permissions;

}