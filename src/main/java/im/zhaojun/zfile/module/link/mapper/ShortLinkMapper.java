package im.zhaojun.zfile.module.link.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接配置表 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    /**
     * 根据短链接 key 查询短链接
     *
     * @param   key
     *          短链接 key
     *
     * @return  短链接信息
     */
    ShortLink findByKey(@Param("key")String key);


    /**
     * 根据存储源 ID 和文件路径查询短链接
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   url
     *          短链接 url
     *
     * @return  短链接信息
     */
    ShortLink findByStorageIdAndUrl(@Param("storageId") Integer storageId, @Param("url") String url);
	
	
	/**
	 * 根据存储源 ID 删除所有数据
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 */
	int deleteByStorageId(@Param("storageId") Integer storageId);
	
}