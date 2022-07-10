package im.zhaojun.zfile.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.admin.model.entity.StorageSourceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 存储源拓展设置 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface StorageSourceConfigMapper extends BaseMapper<StorageSourceConfig> {

    /**
     * 根据存储源 ID 查询存储源拓展配置, 并按照存储源 id 排序
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源拓展配置列表
     */
    List<StorageSourceConfig> findByStorageIdOrderById(@Param("storageId") Integer storageId);


    /**
     * 获取指定存储源的指定参数名称
     *
     * @param   storageId
     *          存储源 id
     *
     * @param   name
     *          参数名
     *
     * @return  参数信息
     */
    StorageSourceConfig findByStorageIdAndName(@Param("storageId") Integer storageId, @Param("name") String name);


    /**
     * 根据存储源 ID 删除存储源拓展配置
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  删除记录数
     */
    int deleteByStorageId(@Param("storageId") Integer storageId);


    /**
     * 批量插入存储源拓展配置
     *
     * @param   list
     *          存储源拓展配置列表
     *
     * @return  插入记录数
     */
    int insertList(@Param("list") List<StorageSourceConfig> list);

}