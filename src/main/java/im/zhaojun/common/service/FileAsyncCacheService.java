package im.zhaojun.common.service;

import cn.hutool.core.util.ObjectUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
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

    public static final String CACHE_FILE_COUNT_KEY = "file-count";

    public static final String CACHE_DIRECTORY_COUNT_KEY = "directory-count";

    @CreateCache(name = "SYSTEM_CONFIG_CACHE_PREFIX", cacheType = CacheType.LOCAL)
    private Cache<String, Integer> cache;
    
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

        boolean enableCache = systemConfigService.getEnableCache();
        if (!enableCache) {
            log.info("当前启用存储策略 {}, 尚未开启缓存, 跳过缓存.", storageStrategy);
            return;
        }

        AbstractFileService fileService = StorageTypeFactory.getStorageTypeService(storageStrategy);

        if (fileService.getIsUnInitialized()) {
            log.info("存储策略 {} 未初始化成功, 跳过启动缓存.", storageStrategy.getDescription());
            return;
        }

        Integer cacheDirectoryCount = cache.get(CACHE_DIRECTORY_COUNT_KEY);
        if (cacheDirectoryCount == null) {
            cacheDirectoryCount = 0;
        }

        Integer cacheFileCount = cache.get(CACHE_FILE_COUNT_KEY);
        if (cacheFileCount == null) {
            cacheFileCount = 0;
        }

        log.info("缓存 {} 所有文件开始", storageStrategy.getDescription());
        long startTime = System.currentTimeMillis();
        try {
            String path = "/";

            FileService currentFileService = systemConfigService.getCurrentFileService();
            List<FileItemDTO> rootFileItems = currentFileService.fileList("/");
            ArrayDeque<FileItemDTO> queue = new ArrayDeque<>(rootFileItems);

            while (!queue.isEmpty()) {
                FileItemDTO fileItemDTO = queue.pop();

                if (FileTypeEnum.FOLDER.equals(fileItemDTO.getType())) {
                    cacheDirectoryCount++;
                }
                if (FileTypeEnum.FILE.equals(fileItemDTO.getType())) {
                    cacheFileCount++;
                }

                log.debug("已缓存 {} 个文件夹", cacheDirectoryCount);
                cache.put(CACHE_DIRECTORY_COUNT_KEY, cacheDirectoryCount);

                log.debug("已缓存 {} 个文件", cacheFileCount);
                cache.put(CACHE_FILE_COUNT_KEY, cacheFileCount);

                if (fileItemDTO.getType() == FileTypeEnum.FOLDER) {
                    String filePath = StringUtils.removeDuplicateSeparator("/" + fileItemDTO.getPath() + "/" + fileItemDTO.getName() + "/");

                    List<FileItemDTO> fileItems = currentFileService.fileList(filePath);
                    queue.addAll(fileItems);
                }
            }
            cache.put(CACHE_DIRECTORY_COUNT_KEY, cacheDirectoryCount);
            cache.put(CACHE_FILE_COUNT_KEY, cacheFileCount);
        } catch (Exception e) {
            log.error("缓存所有文件失败", e);
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        log.info("缓存 {} 所有文件结束, 用时: {} 秒, 文件夹共 {} 个, 文件共 {} 个",
                storageStrategy.getDescription(),
                ( (endTime - startTime) / 1000 ), cacheDirectoryCount, cacheFileCount);
        cacheFinish = true;
    }


    /**
     * 清理缓存的文件/文件夹数量统计
     */
    public void resetCacheCount() {
        cache.remove(CACHE_DIRECTORY_COUNT_KEY);
        cache.remove(CACHE_FILE_COUNT_KEY);
    }

    public Integer getCacheDirectoryCount() {
        return ObjectUtil.defaultIfNull(cache.get(CACHE_DIRECTORY_COUNT_KEY), 0);
    }

    public Integer getCacheFileCount() {
        return ObjectUtil.defaultIfNull(cache.get(CACHE_FILE_COUNT_KEY), 0);
    }

    public boolean isCacheFinish() {
        return cacheFinish;
    }

    public void setCacheFinish(boolean cacheFinish) {
        this.cacheFinish = cacheFinish;
    }
}
