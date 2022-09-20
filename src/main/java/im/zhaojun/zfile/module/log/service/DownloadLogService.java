package im.zhaojun.zfile.module.log.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.module.log.mapper.DownloadLogMapper;
import im.zhaojun.zfile.module.log.model.entity.DownloadLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 下载日志 Service
 *
 * @author zhaojun
 */
@Service
public class DownloadLogService {

	@Resource
	private DownloadLogMapper downloadLogMapper;

	public void save(DownloadLog downloadLog) {
		downloadLogMapper.insert(downloadLog);
	}

	public Page<DownloadLog> selectPage(Page<DownloadLog> pages, QueryWrapper<DownloadLog> queryWrapper) {
		return downloadLogMapper.selectPage(pages, queryWrapper);
	}
	
	public void removeById(Integer id) {
		downloadLogMapper.deleteById(id);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void removeBatchByIds(List<Integer> ids) {
		downloadLogMapper.deleteBatchIds(ids);
	}
	
	public int deleteByStorageKey(String storageKey) {
		return downloadLogMapper.deleteByStorageKey(storageKey);
	}
	
}