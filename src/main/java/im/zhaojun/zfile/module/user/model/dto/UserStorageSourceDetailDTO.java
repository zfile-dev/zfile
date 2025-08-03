package im.zhaojun.zfile.module.user.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class UserStorageSourceDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(title="id")
    private Integer id;

    @Schema(title="用户 id")
    private Integer userId;

    @Schema(title="存储源 ID")
    private Integer storageSourceId;

    /**
     * 相较于 entity 额外查询的字段
     */
    @Schema(title="存储源名称")
    private String storageSourceName;

    /**
     * 相较于 entity 额外查询的字段
     */
    @Schema(title="存储策略类型")
    private StorageTypeEnum storageSourceType;

    @Schema(title="允许访问的基础路径")
    private String rootPath;

    @Schema(title="是否启用")
    private Boolean enable;

    @Schema(title="权限列表")
    @TableField(typeHandler = im.zhaojun.zfile.core.config.mybatis.CollectionStrTypeHandler.class)
    private Set<String> permissions;

}