package im.zhaojun.zfile.config;

import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.service.impl.OneDriveChinaServiceImpl;
import im.zhaojun.zfile.service.impl.OneDriveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Configuration
@EnableScheduling
@Slf4j
public class GlobalScheduleTask {

    @Resource
    private OneDriveServiceImpl oneDriveServiceImpl;

    @Resource
    private OneDriveChinaServiceImpl oneDriveChinaServiceImpl;

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 项目启动 30 秒后, 每 15 分钟执行一次刷新 OneDrive Token 的定时任务.
     */
    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 30)
    public void autoRefreshOneDriveToken() {

        try {
            log.debug("尝试调用 OneDrive 自动刷新 AccessToken 定时任务");

            AbstractBaseFileService currentFileService = systemConfigService.getCurrentFileService();

            if (!(currentFileService instanceof OneDriveServiceImpl
                    || currentFileService instanceof OneDriveChinaServiceImpl)) {
                log.debug("当前启用存储类型, 不是 OneDrive, 跳过自动刷新 AccessToken");
                return;
            }

            if (currentFileService.getIsUnInitialized()) {
                log.debug("当前启用 OneDrive 未初始化成功, 跳过自动刷新 AccessToken");
                return;
            }

            StorageTypeEnum currentStorageTypeEnum = currentFileService.getStorageTypeEnum();

            try {
                refreshOneDriveToken(currentStorageTypeEnum);
            } catch (Exception e) {
                log.debug("刷新 " + currentStorageTypeEnum.getDescription() + " Token 失败.", e);
            }
        } catch (Throwable e) {
            log.debug("尝试调用 OneDrive 自动刷新 AccessToken 定时任务出现未知异常", e);
        }

    }

    /**
     * 调用刷新 OneDrive Token
     */
    public void refreshOneDriveToken(StorageTypeEnum storageType) {
        if (Objects.equals(storageType, StorageTypeEnum.ONE_DRIVE_CHINA)) {
            oneDriveChinaServiceImpl.refreshOneDriveToken();
        } else {
            oneDriveServiceImpl.refreshOneDriveToken();
        }
        log.info("刷新 {} key 时间: {}", storageType.getDescription(), LocalDateTime.now());
    }

}