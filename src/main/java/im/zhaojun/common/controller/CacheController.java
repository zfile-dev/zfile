package im.zhaojun.common.controller;

import im.zhaojun.common.model.dto.CacheConfigDTO;
import im.zhaojun.common.model.dto.ResultBean;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileAsyncCacheService;
import im.zhaojun.common.service.FileCacheService;
import im.zhaojun.common.service.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Set;

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

    @PostMapping("/enable")
    public ResultBean enableCache() throws Exception {
        fileCacheService.enableCache();
        return ResultBean.success();
    }

    @PostMapping("/disable")
    public ResultBean disableCache() throws Exception {
        fileCacheService.disableCache();
        return ResultBean.success();
    }

    @GetMapping("/config")
    public ResultBean cacheConfig() throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        Set<String> cacheKeys = fileService.getCacheKeys();

        CacheConfigDTO cacheConfigDTO = new CacheConfigDTO();
        cacheConfigDTO.setEnableCache(systemConfigService.getEnableCache());
        cacheConfigDTO.setCacheFinish(fileAsyncCacheService.isCacheFinish());
        cacheConfigDTO.setCacheKeys(cacheKeys);
        cacheConfigDTO.setCacheDirectoryCount(fileAsyncCacheService.getCacheDirectoryCount());
        cacheConfigDTO.setCacheFileCount(fileAsyncCacheService.getCacheFileCount());
        return ResultBean.success(cacheConfigDTO);
    }

    @PostMapping("/refresh")
    public ResultBean refreshCache(String key) throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.refreshCache(key);
        return ResultBean.success();
    }

    @PostMapping("/clear")
    public ResultBean clearCache(String key) throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearFileCache();
        return ResultBean.success();
    }

    @PostMapping("/all")
    public ResultBean cacheAll() throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearFileCache();
        fileAsyncCacheService.cacheGlobalFile();
        return ResultBean.success();
    }
}
