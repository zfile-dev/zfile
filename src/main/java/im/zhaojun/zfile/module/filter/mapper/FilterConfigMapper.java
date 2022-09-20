package im.zhaojun.zfile.module.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.filter.model.entity.FilterConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 过滤器配置表 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface FilterConfigMapper extends BaseMapper<FilterConfig> {

    /**
     * 根据存储源 ID 获取存储源配置列表
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源过滤器配置列表
     */
    List<FilterConfig> findByStorageId(@Param("storageId") Integer storageId);


    /**
     * 根据存储源 ID 删除过滤器配置
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  删除条数
     */
    int deleteByStorageId(@Param("storageId") Integer storageId);


    /**
     * 获取所有类型为禁止访问的过滤规则
     *
     * @param   storageId
     *          存储 ID
     *
     * @return  禁止访问的过滤规则列表
     */
    List<FilterConfig> findByStorageIdAndInaccessible(@Param("storageId")Integer storageId);


    /**
     * 获取所有类型为禁止下载的过滤规则
     *
     * @param   storageId
     *          存储 ID
     *
     * @return  禁止下载的过滤规则列表
     */
    List<FilterConfig> findByStorageIdAndDisableDownload(@Param("storageId")Integer storageId);

}