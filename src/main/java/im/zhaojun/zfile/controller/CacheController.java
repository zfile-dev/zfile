package im.zhaojun.zfile.controller;

import im.zhaojun.zfile.cache.ZFileCache;
import im.zhaojun.zfile.model.dto.CacheConfigDTO;
import im.zhaojun.zfile.model.dto.ResultBean;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.service.support.FileAsyncCacheService;
import im.zhaojun.zfile.service.support.FileCacheService;
import im.zhaojun.zfile.service.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin/cache")
public class CacheController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    @Resource
    private FileCacheService fileCacheService;

    @Resource
    private ZFileCache zFileCache;

    @PostMapping("/enable")
    public ResultBean enableCache() {
        fileCacheService.enableCache();
        return ResultBean.success();
    }

    @PostMapping("/disable")
    public ResultBean disableCache() {
        fileCacheService.disableCache();
        return ResultBean.success();
    }

    @GetMapping("/config")
    public ResultBean cacheConfig() {
        AbstractBaseFileService fileService = systemConfigService.getCurrentFileService();
        CacheConfigDTO cacheConfigDTO = new CacheConfigDTO();
        cacheConfigDTO.setEnableCache(systemConfigService.getEnableCache());
        cacheConfigDTO.setCacheFinish(fileAsyncCacheService.isCacheFinish());
        cacheConfigDTO.setCacheKeys(zFileCache.keySet());
        cacheConfigDTO.setCacheDirectoryCount(zFileCache.cacheCount());
        cacheConfigDTO.setLastCacheAutoRefreshDate(zFileCache.getLastCacheAutoRefreshDate());
        return ResultBean.success(cacheConfigDTO);
    }


    @PostMapping("/refresh")
    public ResultBean refreshCache(String key) throws Exception {
        AbstractBaseFileService fileService = systemConfigService.getCurrentFileService();
        fileService.refreshCache(key);
        return ResultBean.success();
    }

    /*
    @PostMapping("/clear")
    public ResultBean clearCache(String key) {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearFileCache();
        return ResultBean.success();
    }


    @PostMapping("/all")
    public ResultBean cacheAll()  {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearFileCache();
        fileAsyncCacheService.cacheGlobalFile();
        return ResultBean.success();
    }
    */
}
