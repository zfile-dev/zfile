package im.zhaojun.common.config;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.service.SystemConfigService;
import im.zhaojun.onedrive.china.service.OneDriveChinaService;
import im.zhaojun.onedrive.china.service.OneDriveServiceChinaImpl;
import im.zhaojun.onedrive.common.model.OneDriveToken;
import im.zhaojun.onedrive.international.service.OneDriveService;
import im.zhaojun.onedrive.international.service.OneDriveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

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
    private OneDriveChinaService oneDriveChinaService;

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 项目启动 30 秒后, 每半小时执行一次刷新 OneDrive Token 的定时任务.
     */
    @Scheduled(fixedRate = 1000 * 60 * 30, initialDelay = 1000 * 30)
    public void autoRefreshOneDriveToken() {

        AbstractFileService currentFileService = systemConfigService.getCurrentFileService();

        if (!(currentFileService instanceof OneDriveServiceImpl
            || currentFileService instanceof OneDriveServiceChinaImpl)) {
            log.debug("当前启用存储类型, 不是 OneDrive, 跳过自动刷新 AccessToken");
            return;
        }

        if (currentFileService.getIsUnInitialized()) {
            log.debug("当前启用 OneDrive 未初始化成功, 跳过自动刷新 AccessToken");
            return;
        }

        try {
            refreshOneDriveToken(StorageTypeEnum.ONE_DRIVE);
        } catch (Exception e) {
            log.debug("刷新 OneDrive Token 失败.", e);
        }

        try {
            refreshOneDriveToken(StorageTypeEnum.ONE_DRIVE_CHINA);
        } catch (Exception e) {
            log.debug("刷新 OneDrive 世纪互联 Token 失败.", e);
        }
    }

    /**
     * 调用刷新 OneDrive Token
     */
    public void refreshOneDriveToken(StorageTypeEnum storageType) {

        OneDriveToken refreshToken;
        if (Objects.equals(storageType, StorageTypeEnum.ONE_DRIVE_CHINA)) {
            refreshToken = oneDriveChinaService.getRefreshToken();
        } else {
            refreshToken = oneDriveService.getRefreshToken();
        }


        if (refreshToken.getAccessToken() == null || refreshToken.getRefreshToken() == null) {
            return;
        }

        StorageConfig accessTokenConfig =
                storageConfigService.selectByTypeAndKey(storageType, StorageConfigConstant.ACCESS_TOKEN_KEY);
        StorageConfig refreshTokenConfig =
                storageConfigService.selectByTypeAndKey(storageType, StorageConfigConstant.REFRESH_TOKEN_KEY);
        accessTokenConfig.setValue(refreshToken.getAccessToken());
        refreshTokenConfig.setValue(refreshToken.getRefreshToken());

        storageConfigService.updateStorageConfig(Arrays.asList(accessTokenConfig, refreshTokenConfig));
        log.info("刷新 {} key 时间: {}", storageType.getDescription(), LocalDateTime.now());
    }
}