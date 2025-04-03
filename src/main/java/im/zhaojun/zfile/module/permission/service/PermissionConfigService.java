package im.zhaojun.zfile.module.permission.service;

import im.zhaojun.zfile.module.permission.mapper.PermissionConfigMapper;
import im.zhaojun.zfile.module.permission.model.entity.PermissionConfig;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 存储源权限 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "permissionConfig")
@Deprecated
public class PermissionConfigService {

	@Resource
	private PermissionConfigMapper permissionConfigMapper;

	/**
	 * 根据存储源 ID 查询权限配置
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  存储源权限配置列表
	 */
	@Deprecated
	public synchronized List<PermissionConfig> findByStorageId(Integer storageId) {
		return ((PermissionConfigService) AopContext.currentProxy()).findByStorageIdNotThreadSafe(storageId);
	}


	/**
	 * 根据存储源 ID 查询权限配置
	 * 提示：受 sqlite 限制, 多线程调用此方法会出现 "[SQLITE_BUSY]  The database file is locked (database is locked)" 错误)
	 * 建议使用 {@link PermissionConfigService#findByStorageId(Integer)} 俩保证所有数据库都是现场安全的。
	 *
	 * @param   storageId
	 *          存储源ID
	 *
	 * @return  存储源权限配置列表
	 */
	@Transactional(rollbackFor = Exception.class)
	@Deprecated
	@Cacheable(key = "#storageId",
			condition = "#storageId != null")
	public List<PermissionConfig> findByStorageIdNotThreadSafe(Integer storageId) {
		// 数据库查询所有权限配置
		List<PermissionConfig> dbResult = permissionConfigMapper.findByStorageId(storageId);
		// 按照权限枚举顺序排序结果
		dbResult.sort(Comparator.comparingInt(permissionConfig -> permissionConfig.getOperator().ordinal()));
		return dbResult;
	}


	/**
	 * 根据存储源 ID 删除权限配置
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 */
	@Deprecated
	@CacheEvict(key = "#storageId")
	public int deleteByStorageId(Integer storageId) {
		int deleteSize = permissionConfigMapper.deleteByStorageId(storageId);
		log.info("删除存储源 ID 为 {} 的权限配置 {} 条", storageId, deleteSize);
		return deleteSize;
	}


	/**
	 * 监听存储源删除事件，根据存储源 id 删除相关的权限设置
	 *
	 * @param   storageSourceDeleteEvent
	 *          存储源删除事件
	 */
	@EventListener
	public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
		Integer storageId = storageSourceDeleteEvent.getId();
		int updateRows = ((PermissionConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);
		if (log.isDebugEnabled()) {
			log.debug("删除存储源 [id {}, name: {}, type: {}] 时，关联删除存储源权限设置(老) {} 条",
					storageId,
					storageSourceDeleteEvent.getName(),
					storageSourceDeleteEvent.getType().getDescription(),
					updateRows);
		}
	}


}