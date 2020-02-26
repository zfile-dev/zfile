package im.zhaojun.common.service;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.List;

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

    @Async
    public void cacheGlobalFile() {
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
        log.info("缓存 {} 所有文件结束, 用时: {} 秒", storageStrategy.getDescription(), ((endTime - startTime) / 1000));
        cacheFinish = true;
    }


    public boolean isCacheFinish() {
        return cacheFinish;
    }

    public void setCacheFinish(boolean cacheFinish) {
        this.cacheFinish = cacheFinish;
    }

}
