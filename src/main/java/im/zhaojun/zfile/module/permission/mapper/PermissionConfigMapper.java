package im.zhaojun.zfile.module.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.permission.model.entity.PermissionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
  * 权限设置 Mapper
  *
 * @author zhaojun
 */
@Mapper
public interface PermissionConfigMapper extends BaseMapper<PermissionConfig> {

    /**
     * 根据存储源 ID 查询权限配置
     *
     * @param   storageId
     *          存储源ID
     *
     * @return  存储源权限配置列表
     */
    List<PermissionConfig> findByStorageId(@Param("storageId") Integer storageId);


    /**
     * 根据存储源 ID 删除权限配置
     *
     * @param   storageId
     *          存储源ID
     *
     * @return  删除记录数
     */
    int deleteByStorageId(@Param("storageId") Integer storageId);
    
}