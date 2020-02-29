package im.zhaojun.common.service;

import im.zhaojun.common.cache.ZFileCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhaojun
 */
@Service
public class FileCacheService {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    @Lazy
    private FileAsyncCacheService fileAsyncCacheService;

    @Resource
    private ZFileCache zFileCache;

    public void enableCache() {
        systemConfigService.updateCacheEnableConfig(true);
        fileAsyncCacheService.cacheGlobalFile();
    }

    public void disableCache() {
        systemConfigService.updateCacheEnableConfig(false);
        zFileCache.clear();
        fileAsyncCacheService.setCacheFinish(false);
        fileAsyncCacheService.stopScheduled();
    }

}
