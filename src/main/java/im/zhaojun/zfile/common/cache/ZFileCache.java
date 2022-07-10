package im.zhaojun.zfile.common.cache;

import cn.hutool.cache.impl.CacheObj;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.admin.model.dto.StorageSourceCacheKey;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ZFile 缓存工具类
 *
 * @author zhaojun
 */
@Service("zFileCache")
@Slf4j
public class ZFileCache {

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private StorageSourceContext storageSourceContext;

    /**
     * 缓存过期时间
     */
    @Value("${zfile.cache.timeout}")
    private long timeout;

    /**
     * 缓存自动刷新间隔
     */
    @Value("${zfile.cache.auto-refresh.interval}")
    private long autoRefreshInterval;

    /**
     * 文件/文件对象缓存.
     *
     * ConcurrentMap<Integer, ConcurrentHashMap<String, List<FileItemDTO>>>
     * ConcurrentMap<storageId, ConcurrentHashMap<key, value>>
     *
     * storageId: 存储源 ID
     * key: 文件夹路径
     * value: 文件夹中内容
     */
    private final ConcurrentMap<Integer, MyTimedCache<StorageSourceCacheKey, List<FileItemResult>>> storageSourcesCache = new ConcurrentHashMap<>();

    /**
     * 系统设置缓存
     */
    private SystemConfigDTO systemConfigCache;


    /**
     * 写入缓存
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   key
     *          文件夹路径
     *
     * @param   value
     *          文件夹中列表
     */
    public synchronized void put(Integer storageId, String key, List<FileItemResult> value) {
        getCacheByStorageId(storageId).put(new StorageSourceCacheKey(storageId, key), value);
    }


    /**
     * 获取指定存储源, 某个文件夹的名称
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   key
     *          文件夹路径
     *
     * @return  存储源中文件夹的内容
     */
    public List<FileItemResult> get(Integer storageId, String key) {
        return getCacheByStorageId(storageId).get(new StorageSourceCacheKey(storageId, key), false);
    }


    /**
     * 清空指定存储源的缓存.
     *
     * @param   storageId
     *          存储源 ID
     */
    public void clear(Integer storageId) {
        if (log.isDebugEnabled()) {
            log.debug("清空存储源所有缓存, storageId: {}", storageId);
        }
        getCacheByStorageId(storageId).clear();
    }


    /**
     * 获取指定存储源中已缓存文件夹数量
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  已缓存文件夹数量
     */
    public int cacheCount(Integer storageId) {
        return getCacheByStorageId(storageId).size();
    }


    /**
     * 指定存储源, 根据文件及文件名查找相关的文件
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   key
     *          搜索键, 可匹配文件夹名称和文件名称.
     *
     * @return  搜索结果, 包含文件夹和文件.
     */
    public List<FileItemResult> find(Integer storageId, String key) {
        return new ArrayList<>();
    }


    private boolean testMatch(String searchKey, boolean ignoreCase, FileItemResult fileItemResult) {
        // 根据是否需要忽略大小写来匹配文件(夹)名
        if (ignoreCase) {
            return StrUtil.containsIgnoreCase(fileItemResult.getName(), searchKey);
        } else {
            return StrUtil.contains(fileItemResult.getName(), searchKey);
        }

    }


    /**
     * 获取所有缓存 key (文件夹名称)
     *
     * @return      所有缓存 key
     */
    public Set<String> keySet(Integer storageId) {
        Iterator<CacheObj<StorageSourceCacheKey, List<FileItemResult>>> cacheObjIterator = getCacheByStorageId(storageId).cacheObjIterator();
        Set<String> keys = new HashSet<>();
        while (cacheObjIterator.hasNext()) {
            keys.add(cacheObjIterator.next().getKey().getKey());
        }
        return keys;
    }


    /**
     * 从缓存中删除指定存储源的某个路径的缓存
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   key
     *          文件夹路径
     */
    public void remove(Integer storageId, String key) {
        getCacheByStorageId(storageId).remove(new StorageSourceCacheKey(storageId, key));
    }


    /**
     * 更新缓存中的系统设置
     *
     * @param   systemConfigCache
     *          系统设置
     */
    public void updateConfig(SystemConfigDTO systemConfigCache) {
        this.systemConfigCache = systemConfigCache;
    }


    /**
     * 从缓存中获取系统设置
     *
     * @return  系统设置
     */
    public SystemConfigDTO getConfig() {
        return this.systemConfigCache;
    }


    /**
     * 清空系统设置缓存
     */
    public void removeConfig() {
        this.systemConfigCache = null;
    }


    /**
     * 获取指定存储源对应的缓存
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源对应的缓存
     */
    private synchronized MyTimedCache<StorageSourceCacheKey, List<FileItemResult>> getCacheByStorageId(Integer storageId) {
        MyTimedCache<StorageSourceCacheKey, List<FileItemResult>> driveCache = storageSourcesCache.get(storageId);
        if (driveCache == null) {
            driveCache = new MyTimedCache<>(timeout * 1000);
            storageSourcesCache.put(storageId, driveCache);
            startAutoCacheRefresh(storageId);
        }
        return driveCache;
    }


    /**
     * 获取指定存储源的缓存命中数
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  缓存命中数
     */
    public long getHitCount(Integer storageId) {
        return getCacheByStorageId(storageId).getHitCount();
    }


    /**
     * 获取指定存储源的缓存未命中数
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  缓存未命中数
     */
    public long getMissCount(Integer storageId) {
        return getCacheByStorageId(storageId).getMissCount();
    }


    /**
     * 开启缓存自动刷新
     *
     * @param   storageId
     *          存储源 ID
     */
    public void startAutoCacheRefresh(Integer storageId) {
        if (log.isDebugEnabled()) {
            log.debug("开启缓存自动刷新 storageId: {}", storageId);
        }
        StorageSource storageSource = storageSourceService.findById(storageId);
        Boolean autoRefreshCache = storageSource.getAutoRefreshCache();
        if (autoRefreshCache != null && autoRefreshCache) {
            MyTimedCache<StorageSourceCacheKey, List<FileItemResult>> driveCache = storageSourcesCache.get(storageId);
            if (driveCache == null) {
                driveCache = new MyTimedCache<>(timeout * 1000);
                storageSourcesCache.put(storageId, driveCache);
            }
            driveCache.schedulePrune(autoRefreshInterval * 1000);
        }
    }


    /**
     * 停止缓存自动刷新
     *
     * @param   storageId
     *          存储源 ID
     */
    public void stopAutoCacheRefresh(Integer storageId) {
        if (log.isDebugEnabled()) {
            log.debug("停止缓存自动刷新 storageId: {}", storageId);
        }
        MyTimedCache<StorageSourceCacheKey, List<FileItemResult>> driveCache = storageSourcesCache.get(storageId);
        if (driveCache != null) {
            driveCache.cancelPruneSchedule();
        }
    }

}