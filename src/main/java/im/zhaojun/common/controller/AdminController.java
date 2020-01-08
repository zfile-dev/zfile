package im.zhaojun.common.controller;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.dto.CacheConfigDTO;
import im.zhaojun.common.model.dto.ResultBean;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileAsyncCacheService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 后台管理
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public ResultBean getConfig() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        return ResultBean.success(systemConfigDTO);
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/config")
    public ResultBean updateConfig(SystemConfigDTO systemConfigDTO) throws Exception {
        StorageTypeEnum currentStorageStrategy = systemConfigService.getCurrentStorageStrategy();

        systemConfigDTO.setId(1);
        systemConfigService.updateSystemConfig(systemConfigDTO);

        if (!Objects.equals(currentStorageStrategy, systemConfigDTO.getStorageStrategy())) {
            refreshStorageStrategy();
        }

        return ResultBean.success();
    }

    /**
     * 修改管理员登陆密码
     */
    @PostMapping("/update-pwd")
    public ResultBean updatePwd(String username, String password) {
        systemConfigService.updateUsernameAndPwd(username, password);
        return ResultBean.success();
    }

    /**
     * 获取指定存储引擎的设置
     * @param storageType   存储引擎
     * @return              所有设置
     */
    @GetMapping("/strategy-form")
    public ResultBean getFormByStorageType(StorageTypeEnum storageType) {
        List<StorageConfig> storageConfigList = storageConfigService.selectStorageConfigByType(storageType);
        return ResultBean.success(storageConfigList);
    }

    @GetMapping("/cache/config")
    public ResultBean cacheConfig() throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        Set<String> cacheKeys = fileService.getCacheKeys();

        CacheConfigDTO cacheConfigDTO = new CacheConfigDTO();
        cacheConfigDTO.setEnableCache(systemConfigService.getEnableCache());
        cacheConfigDTO.setCacheFinish(fileAsyncCacheService.isCacheFinish());
        cacheConfigDTO.setCacheKeys(cacheKeys);

        return ResultBean.success(cacheConfigDTO);
    }

    @PostMapping("/cache/refresh")
    public ResultBean refreshCache(String key) throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.refreshCache(key);
        return ResultBean.success();
    }

    @PostMapping("/cache/clear")
    public ResultBean clearCache(String key) throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearCache();
        return ResultBean.success();
    }

    @PostMapping("/cache/all")
    public ResultBean cacheAll() throws Exception {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearCache();
        fileAsyncCacheService.cacheGlobalFile();
        return ResultBean.success();
    }

    /**
     * 更新存储策略
     */
    public void refreshStorageStrategy() {
        StorageTypeEnum storageStrategy = systemConfigService.getCurrentStorageStrategy();
        refreshStorageStrategy(storageStrategy);
    }

    /**
     * 更新存储策略
     */
    public void refreshStorageStrategy(StorageTypeEnum storageStrategy) {
        if (storageStrategy == null) {
            log.info("尚未配置存储策略.");
        } else {
            AbstractFileService fileService = systemConfigService.getCurrentFileService();
            fileService.init();
            log.info("当前启用存储类型: {}", storageStrategy.getDescription());

            // if 判断是否开启搜索.
            fileAsyncCacheService.cacheGlobalFile();
        }
    }

}
