package im.zhaojun.common.service;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhaojun
 */
@Slf4j
@Service
public class FileAsyncCacheService {

    private boolean cacheFinish;

    @Resource
    private SystemConfigService systemConfigService;

    @Async
    public void cacheGlobalFile() {
        StorageTypeEnum storageStrategy = systemConfigService.getCurrentStorageStrategy();

        if (storageStrategy == null) {
            log.info("尚未配置存储策略. 跳过启动缓存.");
            return;
        }

        AbstractFileService fileService = StorageTypeFactory.getStorageTypeService(storageStrategy);
        log.info("缓存 {} 所有文件开始", storageStrategy.getDescription());
        long startTime = System.currentTimeMillis();
        try {
            if (fileService.getIsInitialized()) {
                fileService.selectAllFileList();
            }
        } catch (Exception e) {
            log.error("缓存所有文件失败", e);
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        log.info("缓存 {} 所有文件结束, 用时: {} 秒", storageStrategy.getDescription(), ( (endTime - startTime) / 1000 ));
        cacheFinish = true;
    }

    public boolean isCacheFinish() {
        return cacheFinish;
    }

    public void setCacheFinish(boolean cacheFinish) {
        this.cacheFinish = cacheFinish;
    }
}
