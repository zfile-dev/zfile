package im.zhaojun.common.service;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FileAsyncCacheService {

    private static final Logger log = LoggerFactory.getLogger(FileAsyncCacheService.class);

    @Resource
    private SystemConfigService systemConfigService;

    @Async
    public void cacheGlobalFile() {
        StorageTypeEnum storageStrategy = systemConfigService.getCurrentStorageStrategy();

        if (storageStrategy == null) {
            log.info("尚未配置存储策略. 跳过启动缓存.");
            return;
        }

        FileService fileService = StorageTypeFactory.getStorageTypeService(storageStrategy);
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
    }
}
