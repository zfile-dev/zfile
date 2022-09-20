package im.zhaojun.zfile.module.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 系统配置 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 获取所有系统设置
     *
     * @return  系统设置列表
     */
    List<SystemConfig> findAll();


    /**
     * 根据系统设置名称获取设置信息
     *
     * @param   name
     *          系统设置名称
     *
     * @return  系统设置信息
     */
    SystemConfig findByName(@Param("name")String name);


    /**
     * 批量保存系统设置
     *
     * @param   list
     *          系统设置列表
     *
     * @return  保存记录数
     */
    int saveAll(@Param("list")List<SystemConfig> list);

}