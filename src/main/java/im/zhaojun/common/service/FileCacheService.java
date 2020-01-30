package im.zhaojun.common.service;

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

    public void enableCache() {
        systemConfigService.updateCacheEnableConfig(true);

        AbstractFileService currentFileService = systemConfigService.getCurrentFileService();
        currentFileService.openCacheAutoRefresh();
        fileAsyncCacheService.cacheGlobalFile();
    }


    public void disableCache() throws Exception {
        systemConfigService.updateCacheEnableConfig(false);

        AbstractFileService currentFileService = systemConfigService.getCurrentFileService();
        currentFileService.clearFileCache();
        fileAsyncCacheService.resetCacheCount();
    }

}
