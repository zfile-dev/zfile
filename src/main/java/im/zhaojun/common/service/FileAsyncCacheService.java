package im.zhaojun.common.service;

import im.zhaojun.common.cache.ZFileCache;
import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaojun
 */
@Slf4j
@Service
public class FileAsyncCacheService {

    public static final String CACHE_PROCESS_PREFIX = "zfile-process-cache:";

    private boolean cacheFinish;

    @Resource
    private SystemConfigService systemConfigService;

    private volatile boolean stopFlag = false;

    @Resource
    private ZFileCache zFileCache;

    @Value("${zfile.cache.auto-refresh.enable}")
    protected boolean enableAutoRefreshCache;

    @Value("${zfile.cache.auto-refresh.delay}")
    protected Long delay;

    @Value("${zfile.cache.auto-refresh.interval}")
    protected Long interval;

    @Async
    public void cacheGlobalFile() {
        stopFlag = false;
        StorageTypeEnum storageStrategy = systemConfigService.getCurrentStorageStrategy();

        if (storageStrategy == null) {
            log.debug("尚未配置存储策略. 跳过启动缓存.");
            return;
        }

        boolean enableCache = systemConfigService.getEnableCache();
        if (!enableCache) {
            log.debug("存储策略 {} 未启用缓存, 跳过缓存.", storageStrategy.getDescription());
            return;
        }

        AbstractFileService fileService = StorageTypeFactory.getStorageTypeService(storageStrategy);

        if (fileService.getIsUnInitialized()) {
            log.debug("存储策略 {} 未初始化成功, 跳过缓存.", storageStrategy.getDescription());
            return;
        }

        Integer cacheDirectoryCount = 0;

        log.info("缓存 {} 所有文件开始", storageStrategy.getDescription());
        long startTime = System.currentTimeMillis();
        try {
            FileService currentFileService = systemConfigService.getCurrentFileService();
            List<FileItemDTO> rootFileItems = currentFileService.fileList("/");
            ArrayDeque<FileItemDTO> queue = new ArrayDeque<>(rootFileItems);

            while (!queue.isEmpty()) {
                FileItemDTO fileItemDTO = queue.pop();

                if (stopFlag) {
                    zFileCache.clear();
                    break;
                }

                if (fileItemDTO.getType() == FileTypeEnum.FOLDER) {
                    String filePath = StringUtils.removeDuplicateSeparator("/" + fileItemDTO.getPath() + "/" + fileItemDTO.getName() + "/");

                    List<FileItemDTO> fileItems = currentFileService.fileList(filePath);
                    queue.addAll(fileItems);
                }
            }
        } catch (Exception e) {
            log.error("缓存所有文件失败", e);
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();

        if (stopFlag) {
            log.info("缓存 {} 所有文件被强制结束, 用时: {} 秒", storageStrategy.getDescription(), ((endTime - startTime) / 1000));
            cacheFinish = false;
            stopFlag = false;
        } else {
            log.info("缓存 {} 所有文件结束, 用时: {} 秒", storageStrategy.getDescription(), ((endTime - startTime) / 1000));
            enableCacheAutoRefreshTask();
            cacheFinish = true;
            stopFlag = false;
        }
    }

    private void enableCacheAutoRefreshTask() {
        StorageTypeEnum currentStorageStrategy = systemConfigService.getCurrentStorageStrategy();

        if (enableAutoRefreshCache) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                zFileCache.setLastCacheAutoRefreshDate(new Date());

                boolean enableCache = systemConfigService.getEnableCache();

                if (!enableCache) {
                    log.debug("当前存储引擎未开启缓存, 跳过自动刷新缓存");
                    zFileCache.clear();
                    return;
                }

                log.debug("开始调用自动刷新缓存");

                Set<String> keySet = zFileCache.keySet();

                ArrayList<String> keys = new ArrayList<>(keySet);


                for (String key : keys) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (stopFlag) {
                        break;
                    }

                    zFileCache.remove(key);
                    AbstractFileService currentFileService = systemConfigService.getCurrentFileService();
                    try {
                        if (Objects.equals(currentStorageStrategy, systemConfigService.getCurrentStorageStrategy())) {
                            currentFileService.fileList(key);
                        }
                    } catch (Exception e) {
                        log.error("刷新过程中出错 : [" + key + "]", e);
                    }
                }

                if (stopFlag) {
                    log.debug("检测到停止 [{}] 缓存指令, 已停止自动刷新任务", currentStorageStrategy);
                    scheduledExecutorService.shutdownNow();
                    stopFlag = false;
                } else {
                    log.debug("自动刷新缓存完成");
                }
            }, delay, interval, TimeUnit.SECONDS);
        }
    }

    public void stopScheduled() {
        this.stopFlag = true;
    }

    public void enableScheduled() {
        this.stopFlag = false;
    }

    public boolean isCacheFinish() {
        return cacheFinish;
    }

    public void setCacheFinish(boolean cacheFinish) {
        this.cacheFinish = cacheFinish;
    }

}
