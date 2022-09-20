package im.zhaojun.zfile.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import im.zhaojun.zfile.module.log.model.entity.DownloadLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 下载日志 Mapper 接口
 *
 * @author zhaojun
 */
@Mapper
public interface DownloadLogMapper extends BaseMapper<DownloadLog> {
	
	/**
	 * 根据存储源 KEY 删除所有数据
	 *
	 * @param 	storageKey
	 * 			存储源 KEY
	 */
	int deleteByStorageKey(String storageKey);
	
}