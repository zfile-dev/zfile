package im.zhaojun.zfile.cache;

import cn.hutool.cache.impl.CacheObj;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.repository.DriverConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ZFile 缓存类
 *
 * @author zhaojun
 */
@Component
@Slf4j
public class ZFileCache {

    @Resource
    private DriverConfigRepository driverConfigRepository;

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
     * ConcurrentMap<driveId, ConcurrentHashMap<key, value>>
     *
     * driveId: 驱动器 ID
     * key: 文件夹路径
     * value: 文件夹中内容
     */
    private ConcurrentMap<Integer, MyTimedCache<DriveCacheKey, List<FileItemDTO>>> drivesCache = new ConcurrentHashMap<>();

    /**
     * 系统设置缓存
     */
    private SystemConfigDTO systemConfigCache;


    /**
     * 写入缓存
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   key
     *          文件夹路径
     *
     * @param   value
     *          文件夹中列表
     */
    public synchronized void put(Integer driveId, String key, List<FileItemDTO> value) {
        getCacheByDriveId(driveId).put(new DriveCacheKey(driveId, key), value);
    }


    /**
     * 获取指定驱动器, 某个文件夹的名称
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   key
     *          文件夹路径
     *
     * @return  驱动器中文件夹的内容
     */
    public List<FileItemDTO> get(Integer driveId, String key) {
        return getCacheByDriveId(driveId).get(new DriveCacheKey(driveId, key), false);
    }


    /**
     * 清空指定驱动器的缓存.
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void clear(Integer driveId) {
        if (log.isDebugEnabled()) {
            log.debug("清空驱动器所有缓存, driveId: {}", driveId);
        }
        getCacheByDriveId(driveId).clear();
    }


    /**
     * 获取指定驱动器中已缓存文件夹数量
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  已缓存文件夹数量
     */
    public int cacheCount(Integer driveId) {
        return getCacheByDriveId(driveId).size();
    }


    /**
     * 指定驱动器, 根据文件及文件名查找相关的文件
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   key
     *          搜索键, 可匹配文件夹名称和文件名称.
     *
     * @return  搜索结果, 包含文件夹和文件.
     */
    public List<FileItemDTO> find(Integer driveId, String key) {
        List<FileItemDTO> result = new ArrayList<>();

        DriveConfig driveConfig = driverConfigRepository.getOne(driveId);
        boolean searchContainEncryptedFile = driveConfig.getSearchContainEncryptedFile();
        boolean ignoreCase = driveConfig.getSearchIgnoreCase();

        for (List<FileItemDTO> fileItemList : getCacheByDriveId(driveId)) {
            // 过滤加密文件
            if (!searchContainEncryptedFile && isEncryptedFolder(fileItemList)) {
                continue;
            }

            for (FileItemDTO fileItemDTO : fileItemList) {
                boolean testResult;

                // 根据是否需要忽略大小写来匹配文件(夹)名
                if (ignoreCase) {
                    testResult = StrUtil.containsIgnoreCase(fileItemDTO.getName(), key);
                } else {
                    testResult = fileItemDTO.getName().contains(key);
                }

                if (testResult) {
                    result.add(fileItemDTO);
                }
            }
        }
        return result;
    }


    /**
     * 获取所有缓存 key (文件夹名称)
     *
     * @return      所有缓存 key
     */
    public Set<String> keySet(Integer driveId) {
        Iterator<CacheObj<DriveCacheKey, List<FileItemDTO>>> cacheObjIterator = getCacheByDriveId(driveId).cacheObjIterator();
        Set<String> keys = new HashSet<>();
        while (cacheObjIterator.hasNext()) {
            keys.add(cacheObjIterator.next().getKey().getKey());
        }
        return keys;
    }


    /**
     * 从缓存中删除指定驱动器的某个路径的缓存
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   key
     *          文件夹路径
     */
    public void remove(Integer driveId, String key) {
        getCacheByDriveId(driveId).remove(new DriveCacheKey(driveId, key));
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
     * 判断此文件夹是否为加密文件夹 (包含)
     *
     * @param   list
     *          文件夹中的内容
     *
     * @return  返回此文件夹是否是加密的 ().
     */
    private boolean isEncryptedFolder(List<FileItemDTO> list) {
        // 遍历文件判断是否包含
        for (FileItemDTO fileItemDTO : list) {
            if (Objects.equals(ZFileConstant.PASSWORD_FILE_NAME, fileItemDTO.getName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取指定驱动器对应的缓存
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  驱动器对应的缓存
     */
    private synchronized MyTimedCache<DriveCacheKey, List<FileItemDTO>> getCacheByDriveId(Integer driveId) {
        MyTimedCache<DriveCacheKey, List<FileItemDTO>> driveCache = drivesCache.get(driveId);
        if (driveCache == null) {
            driveCache = new MyTimedCache<>(timeout * 1000);
            drivesCache.put(driveId, driveCache);
            startAutoCacheRefresh(driveId);
        }
        return driveCache;
    }


    /**
     * 获取指定驱动器的缓存命中数
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  缓存命中数
     */
    public int getHitCount(Integer driveId) {
        return getCacheByDriveId(driveId).getHitCount();
    }


    /**
     * 获取指定驱动器的缓存未命中数
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  缓存未命中数
     */
    public int getMissCount(Integer driveId) {
        return getCacheByDriveId(driveId).getMissCount();
    }


    /**
     * 开启缓存自动刷新
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void startAutoCacheRefresh(Integer driveId) {
        if (log.isDebugEnabled()) {
            log.debug("开启缓存自动刷新 driveId: {}", driveId);
        }
        DriveConfig driveConfig = driverConfigRepository.findById(driveId).get();
        Boolean autoRefreshCache = driveConfig.getAutoRefreshCache();
        if (autoRefreshCache != null && autoRefreshCache) {
            MyTimedCache<DriveCacheKey, List<FileItemDTO>> driveCache = drivesCache.get(driveId);
            if (driveCache == null) {
                driveCache = new MyTimedCache<>(timeout * 1000);
                drivesCache.put(driveId, driveCache);
            }
            driveCache.schedulePrune(autoRefreshInterval * 1000);
        }
    }


    /**
     * 停止缓存自动刷新
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void stopAutoCacheRefresh(Integer driveId) {
        if (log.isDebugEnabled()) {
            log.debug("停止缓存自动刷新 driveId: {}", driveId);
        }
        MyTimedCache<DriveCacheKey, List<FileItemDTO>> driveCache = drivesCache.get(driveId);
        if (driveCache != null) {
            driveCache.cancelPruneSchedule();
        }
    }

}