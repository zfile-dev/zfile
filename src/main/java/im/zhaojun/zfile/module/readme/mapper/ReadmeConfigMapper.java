package im.zhaojun.zfile.module.readme.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.readme.model.entity.ReadmeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 存储源文档配置表 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface ReadmeConfigMapper extends BaseMapper<ReadmeConfig> {


	/**
	 * 根据存储源 ID 查询文档配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  存储源文档配置列表
	 */
	List<ReadmeConfig> findByStorageId(@Param("storageId") Integer storageId);


	/**
	 * 根据存储源 ID 删除文档配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  删除记录数
	 */
	int deleteByStorageId(@Param("storageId") Integer storageId);

}