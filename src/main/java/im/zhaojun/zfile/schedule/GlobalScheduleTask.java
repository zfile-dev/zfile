package im.zhaojun.zfile.schedule;

import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.DriveConfigService;
import im.zhaojun.zfile.service.base.AbstractOneDriveServiceBase;
import im.zhaojun.zfile.service.impl.OneDriveChinaServiceImpl;
import im.zhaojun.zfile.service.impl.OneDriveServiceImpl;
import im.zhaojun.zfile.context.DriveContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计划任务工具类
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
    private DriveConfigService driveConfigService;

    @Resource
    private DriveContext driveContext;

    /**
     * 项目启动 30 秒后, 每 15 分钟执行一次刷新 OneDrive Token 的定时任务.
     */
    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 1000 * 30)
    public void autoRefreshOneDriveToken() {

        try {
            log.debug("尝试调用 OneDrive 自动刷新 AccessToken 定时任务");

            List<DriveConfig> driveConfigList = driveConfigService.findByType(StorageTypeEnum.ONE_DRIVE);
            driveConfigList.addAll(driveConfigService.findByType(StorageTypeEnum.ONE_DRIVE_CHINA));

            driveConfigList.forEach(driveConfig -> {
                StorageTypeEnum storageType = driveConfig.getType();
                String name = driveConfig.getName();

                try {
                    AbstractOneDriveServiceBase driveService = (AbstractOneDriveServiceBase) driveContext.getDriveService(driveConfig.getId());
                    driveService.refreshOneDriveToken();
                    log.info("刷新驱动器 {}, {} key 时间: {}", name, storageType.getDescription(), LocalDateTime.now());
                } catch (Exception e) {
                    log.debug("刷新驱动器 " + name + " Token 失败.", e);
                }

            });

        } catch (Throwable e) {
            log.debug("尝试调用 OneDrive 自动刷新 AccessToken 定时任务出现未知异常", e);
        }

    }

}