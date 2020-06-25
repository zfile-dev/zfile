package im.zhaojun.zfile.cache;

import cn.hutool.cache.impl.CacheObj;
import cn.hutool.cache.impl.TimedCache;
import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author zhaojun
 */
@Slf4j
public class MyTimedCache<K, V> extends TimedCache<K, V> {

    private DriveContext driveContext;

    public MyTimedCache(long timeout) {
        super(timeout);
    }

    public MyTimedCache(long timeout, Map<K, CacheObj<K, V>> map) {
        super(timeout, map);
    }

    @Override
    protected void onRemove(K key, V cachedObject) {
        log.debug("尝试刷新缓存: " + key);
        if (driveContext == null) {
            driveContext = SpringContextHolder.getBean(DriveContext.class);
        }
        DriveCacheKey cacheKey = (DriveCacheKey) key;
        AbstractBaseFileService baseFileService = driveContext.get(cacheKey.getDriveId());
        try {
            baseFileService.fileList(cacheKey.getKey());
        } catch (Exception e) {
            log.error("尝试刷新驱动器 {} 的 {} 失败, ", cacheKey.getDriveId(), cacheKey.getKey());
            e.printStackTrace();
        }
    }
}
