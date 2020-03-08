package im.zhaojun.zfile.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ZipUtil;
import im.zhaojun.zfile.config.StorageTypeFactory;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.support.SystemMonitorInfo;
import im.zhaojun.zfile.model.dto.ResultBean;
import im.zhaojun.zfile.model.dto.StorageStrategyDTO;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.service.support.FileAsyncCacheService;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

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

    private ScheduledExecutorService scheduledExecutorService;

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
        if (!Objects.equals(currentStorageStrategy, systemConfigDTO.getStorageStrategy())) {
            if (systemConfigService.getEnableCache()) {
                return ResultBean.error("不支持缓存开启状态下, 切换存储策略, 请先手动关闭缓存");
            }
            log.info("已将存储策略由 {} 切换为 {}",
                    currentStorageStrategy.getDescription(),
                    systemConfigDTO.getStorageStrategy().getDescription());
            refreshStorageStrategy();
        }

        systemConfigDTO.setId(1);
        systemConfigService.updateSystemConfig(systemConfigDTO);

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
     * 获取指定存储策略的设置
     * @param storageType   存储策略
     * @return              所有设置
     */
    @GetMapping("/strategy-form")
    public ResultBean getFormByStorageType(StorageTypeEnum storageType) {
        List<StorageConfig> storageConfigList = storageConfigService.selectStorageConfigByType(storageType);
        return ResultBean.success(storageConfigList);
    }

    /**
     * 返回支持的存储引擎.
     */
    @GetMapping("/support-strategy")
    public ResultBean supportStrategy() {
        List<StorageStrategyDTO> result = new ArrayList<>();
        StorageTypeEnum[] values = StorageTypeEnum.values();
        for (StorageTypeEnum value : values) {
            AbstractBaseFileService storageTypeService = StorageTypeFactory.getStorageTypeService(value);
            result.add(new StorageStrategyDTO(value.getKey(),
                                    value.getDescription(),
                                    storageTypeService.getIsInitialized()));
        }
        return ResultBean.successData(result);
    }

    /**
     * 保存存储策略
     * @param storageStrategyConfig     保存表单值
     * @param storageStrategy           所属策略
     * @return                          操作结果
     */
    @PostMapping("/storage-strategy")
    public ResultBean save(@RequestParam Map<String, String> storageStrategyConfig, StorageTypeEnum storageStrategy) {
        // 保存设置.
        List<StorageConfig> storageConfigList = storageConfigService.selectStorageConfigByType(storageStrategy);
        for (StorageConfig storageConfig : storageConfigList) {
            String key = storageConfig.getKey();
            String value = storageStrategyConfig.get(key);
            storageConfig.setValue(value);
        }
        storageConfigService.updateStorageConfig(storageConfigList);

        // 获取当前修改的存储策略 Service, 尝试调用初始化.
        AbstractBaseFileService updateStorageStrategyService = StorageTypeFactory.getStorageTypeService(storageStrategy);
        updateStorageStrategyService.init();

        // 如果修改的为当前启用的缓存, 则重新进行缓存.
        StorageTypeEnum currentStorageStrategy = systemConfigService.getCurrentStorageStrategy();
        if (Objects.equals(storageStrategy, currentStorageStrategy)) {
            if (log.isDebugEnabled()) {
                log.debug("检测到更新了当前启用的存储策略 {}, 已清理缓存.", currentStorageStrategy);
            }

            AbstractBaseFileService fileService = systemConfigService.getCurrentFileService();
            fileService.clearFileCache();
            fileAsyncCacheService.cacheGlobalFile();
        }

        // 返回是否初始化成功.
        if (updateStorageStrategyService.getIsInitialized()) {
            return ResultBean.success();
        } else {
            return ResultBean.error("保存成功, 但尝试初始化异常, 请检查设置.");
        }
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
    private void refreshStorageStrategy(StorageTypeEnum storageStrategy) {
        if (storageStrategy == null) {
            log.info("尚未配置存储策略.");
        } else {
            AbstractBaseFileService fileService = systemConfigService.getCurrentFileService();
            fileService.init();
            fileService.clearFileCache();
            log.info("切换至存储类型: {}", storageStrategy.getDescription());
            fileAsyncCacheService.cacheGlobalFile();
        }
    }

    /**
     * 系统日志下载
     */
    @GetMapping("/log")
    public ResponseEntity<Object> downloadLog(HttpServletResponse response) {
        String userHome = System.getProperty("user.home");
        File fileZip = ZipUtil.zip(userHome + "/.zfile/logs");
        String currentDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        return FileUtil.export(fileZip, "ZFile 诊断日志 - " + currentDate + ".zip");
    }

    /**
     * 获取系统监控信息
     */
    @GetMapping("monitor")
    public ResultBean monitor() {
        return ResultBean.success(new SystemMonitorInfo());
    }

}
