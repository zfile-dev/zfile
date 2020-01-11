package im.zhaojun.common.service;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhaojun
 * @date 2020/1/11 14:08
 */
@Service
public class FileCacheService {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
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
