package im.zhaojun.zfile.module.link.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

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
	 * 根据存储源 ID 删除所有数据
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 */
	int deleteByStorageId(@Param("storageId") Integer storageId);

	/**
	 * 根据存储源 ID 和 URL 查询短链接
	 */
	ShortLink findByStorageIdAndUrl(@Param("storageId") Integer storageId,
									@Param("url") String url,
									@Nullable @Param("expireDate") Date expireDate);

	/**
	 * 删除过期的短链接
	 *
	 * @return 删除的行数
	 */
	int deleteExpireLink();

}