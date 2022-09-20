package im.zhaojun.zfile.module.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 存储源基本配置 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface StorageSourceMapper extends BaseMapper<StorageSource> {

    /**
     * 获取所有已启用的存储源, 并按照存储源排序值排序
     *
     * @return  存储源列表
     */
    List<StorageSource> findListByEnableOrderByOrderNum();


    /**
     * 获取所有存储源, 并按照存储源排序值排序
     *
     * @return  存储源列表
     */
    List<StorageSource> findAllOrderByOrderNum();


    /**
     * 获取存储源 ID 最大值
     *
     * @return 存储源 ID 最大值
     */
    Integer selectMaxId();


    /**
     * 根据存储源类型获取存储源列表
     *
     * @param   type
     *          存储源类型
     *
     * @return  存储源列表
     */
    List<StorageSource> findByType(@Param("type") StorageTypeEnum type);


    /**
     * 根据存储源 ID 设置排序值
     *
     * @param   orderNum
     *          排序值
     *
     * @param   id
     *          存储源 ID
     */
    void updateSetOrderNumById(@Param("orderNum") int orderNum, @Param("id") Integer id);


    /**
     * 根据存储源 key 获取存储源
     *
     * @param   storageKey
     *          存储源 key
     *
     * @return  存储源信息
     */
	StorageSource findByStorageKey(@Param("storageKey") String storageKey);


    /**
     * 根据存储源 key 获取存储源 id
     *
     * @param   storageKey
     *          存储源 key
     *
     * @return  存储源 id
     */
    Integer findIdByStorageKey(@Param("storageKey") String storageKey);


    /**
     * 根据存储源 id 获取存储源 key
     *
     * @param   id
     *          存储源 id
     *
     * @return  存储源 key
     */
    String findKeyById(@Param("id")Integer id);

}