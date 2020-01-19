package im.zhaojun.common.config;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.service.SystemConfigService;
import im.zhaojun.onedrive.service.OneDriveService;
import im.zhaojun.onedrive.service.OneDriveServiceImpl;
import im.zhaojun.onedrive.service.OneDriveToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author zhaojun
 */
@Configuration
@EnableScheduling
@Slf4j
public class GlobalScheduleTask {

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private OneDriveService oneDriveService;

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 项目启动 30 秒后, 每半小时执行一次刷新 OneDrive Token 的定时任务.
     */
    @Scheduled(fixedRate = 1000 * 60 * 30, initialDelay = 1000 * 30)
    public void autoRefreshOneDriveToken() {

        AbstractFileService currentFileService = systemConfigService.getCurrentFileService();

        if (!(currentFileService instanceof OneDriveServiceImpl)) {
            log.debug("当前启用存储类型, 不是 OneDrive, 跳过自动刷新 AccessToken");
            return;
        }

        if (currentFileService.getIsUnInitialized()) {
            log.debug("当前启用 OneDrive 未初始化成功, 跳过自动刷新 AccessToken");
            return;
        }

        refreshOneDriveToken();
    }

    /**
     * 调用刷新 OneDrive Token
     */
    public void refreshOneDriveToken() {
        OneDriveToken refreshToken = oneDriveService.getRefreshToken();

        StorageConfig accessTokenConfig =
                storageConfigService.selectByTypeAndKey(StorageTypeEnum.ONE_DRIVE, StorageConfigConstant.ACCESS_TOKEN_KEY);
        StorageConfig refreshTokenConfig =
                storageConfigService.selectByTypeAndKey(StorageTypeEnum.ONE_DRIVE, StorageConfigConstant.REFRESH_TOKEN_KEY);
        accessTokenConfig.setValue(refreshToken.getAccessToken());
        refreshTokenConfig.setValue(refreshToken.getRefreshToken());

        storageConfigService.updateStorageConfig(Arrays.asList(accessTokenConfig, refreshTokenConfig));
        log.info("刷新 OneDrive key 时间: " + LocalDateTime.now());
    }
}