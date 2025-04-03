package im.zhaojun.zfile.module.permission.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;
import lombok.Data;

import java.io.Serializable;

 /**
  * 权限设置表
 * @author zhaojun
 */
@Data
@TableName(value = "`permission_config`")
public class PermissionConfig implements Serializable {
    
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 操作
     */
    @TableField(value = "`operator`")
    private FileOperatorTypeEnum operator;

    /**
     * 允许管理员操作
     */
    @TableField(value = "`allow_admin`")
    private Boolean allowAdmin;

    /**
     * 允许匿名用户操作
     */
    @TableField(value = "`allow_anonymous`")
    private Boolean allowAnonymous;

    /**
     * 存储源 ID
     */
    @TableField(value = "`storage_id`")
    private Integer storageId;

    private static final long serialVersionUID = 1L;
    
    public static PermissionConfig getDefaultInstance(Integer storageId, FileOperatorTypeEnum operator) {
        PermissionConfig permissionConfig = new PermissionConfig();
        permissionConfig.storageId = storageId;
        permissionConfig.operator = operator;
    
        FileOperatorTypeDefaultValueDTO defaultPermissionValue = operator.getDefaultValue(storageId);
        permissionConfig.allowAdmin = defaultPermissionValue.isAllowAdmin();
        permissionConfig.allowAnonymous = defaultPermissionValue.isAllowAnonymous();
        
        return permissionConfig;
    }

}