package im.zhaojun.common.controller;

import im.zhaojun.common.model.StorageConfig;
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
    @PostMapping("/update-pwd")
    public ResultBean updatePwd(String username, String password) {
        systemConfigService.updateUsernameAndPwd(username, password);
        return ResultBean.success();
    }

    /**
     * 更新系统配置
     */
    @PostMapping("/config")
    public ResultBean updateConfig(SystemConfigDTO systemConfigDTO) throws Exception {
        StorageTypeEnum currentStorageStrategy = systemConfigService.getCurrentStorageStrategy();

        systemConfigDTO.setId(1);
        systemConfigService.updateSystemConfig(systemConfigDTO);

        if (!currentStorageStrategy.equals(systemConfigDTO.getStorageStrategy())) {
            refreshStorageStrategy();
        }

        return ResultBean.success();
    }


    @GetMapping("/strategy-form")
    public ResultBean getFormByStorageType(StorageTypeEnum storageType) {
        List<StorageConfig> storageConfigList = storageConfigService.selectStorageConfigByType(storageType);
        return ResultBean.success(storageConfigList);
    }

    /**
     * 清理当前启用的存储引擎的缓存
     */
    @PostMapping("/clear-cache")
    public ResultBean clearCache() {
        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        fileService.clearCache();
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
