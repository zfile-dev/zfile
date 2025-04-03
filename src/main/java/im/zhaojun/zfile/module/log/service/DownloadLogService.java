package im.zhaojun.zfile.module.log.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import im.zhaojun.zfile.module.link.event.DeleteExpireLinkEvent;
import im.zhaojun.zfile.module.log.mapper.DownloadLogMapper;
import im.zhaojun.zfile.module.log.model.entity.DownloadLog;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 下载日志 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class DownloadLogService {

	@Resource
	private DownloadLogMapper downloadLogMapper;

	public void save(DownloadLog downloadLog) {
		downloadLogMapper.insert(downloadLog);
	}

	public Page<DownloadLog> selectPage(Page<DownloadLog> pages, Wrapper<DownloadLog> queryWrapper) {
		return downloadLogMapper.selectPage(pages, queryWrapper);
	}

	public void removeById(Integer id) {
		downloadLogMapper.deleteById(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void removeBatchByIds(List<Integer> ids) {
		downloadLogMapper.deleteBatchIds(ids);
	}

	public void deleteByQueryWrapper(Wrapper<DownloadLog> queryWrapper) {
		downloadLogMapper.delete(queryWrapper);
	}

	public int deleteByStorageKey(String storageKey) {
		int deleteSize = downloadLogMapper.deleteByStorageKey(storageKey);
		log.info("删除存储源 ID 为 {} 的直/短链下载日志 {} 条", storageKey, deleteSize);
		return deleteSize;
	}

	/**
	 * 监听存储源删除事件，根据存储源 id 删除相关的下载日志
	 *
	 * @param   storageSourceDeleteEvent
	 *          存储源删除事件
	 */
	@EventListener
	public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
		String storageKey = storageSourceDeleteEvent.getKey();
		int updateRows = ((DownloadLogService) AopContext.currentProxy()).deleteByStorageKey(storageKey);
		if (log.isDebugEnabled()) {
			log.debug("删除存储源 [id {}, key: {}, name: {}, type: {}] 时，关联删除存储源直/短链下载日志 {} 条",
					storageSourceDeleteEvent.getId(),
					storageKey,
					storageSourceDeleteEvent.getName(),
					storageSourceDeleteEvent.getType().getDescription(),
					updateRows);
		}
	}

	/**
	 * 删除过期下载日志
	 *
	 * @return  删除的条数
	 */
	public int deleteExpireShortLinkLog() {
		return downloadLogMapper.deleteExpireShortLinkLog();
	}

	@EventListener(classes = DeleteExpireLinkEvent.class)
	public void deleteExpireShortLinkLog(DeleteExpireLinkEvent event) {
		int updateRows = deleteExpireShortLinkLog();
		log.info("删除过期短链关联删除日志 {} 条", updateRows);
	}

}