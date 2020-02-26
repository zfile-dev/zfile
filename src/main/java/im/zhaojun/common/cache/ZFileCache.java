package im.zhaojun.common.cache;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhaojun
 */
@Component
public class ZFileCache {

    private ConcurrentMap<String, List<FileItemDTO>> fileCache = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Integer> fileCountCache = new ConcurrentHashMap<>();

    private SystemConfigDTO systemConfigCache;

    public static final String CACHE_FILE_COUNT_KEY = "file-count";

    public static final String CACHE_DIRECTORY_COUNT_KEY = "directory-count";

    public synchronized void put(String key, List<FileItemDTO> value) {
        for (FileItemDTO fileItemDTO : value) {
            if (FileTypeEnum.FILE.equals(fileItemDTO.getType())) {
                incrCacheFileCount();
            } else {
                incrCacheDirectoryCount();
            }
        }
        fileCache.put(key, value);
    }

    public List<FileItemDTO> get(String key) {
        return fileCache.get(key);
    }

    public void clear() {
        fileCache.clear();
        fileCountCache.clear();
    }

    public long cacheCount() {
        return fileCache.size();
    }

    public List<FileItemDTO> find(String key, boolean ignoreCase) {
        List<FileItemDTO> result = new ArrayList<>();

        Collection<List<FileItemDTO>> values = fileCache.values();
        for (List<FileItemDTO> fileItemList : values) {
            for (FileItemDTO fileItemDTO : fileItemList) {
                boolean testResult;

                if (ignoreCase) {
                    testResult = StrUtil.containsIgnoreCase(fileItemDTO.getName(), key);
                } else {
                    testResult = fileItemDTO.getName().contains(key);
                }

                if (testResult) {
                    result.add(fileItemDTO);
                }
            }
        }
        return result;
    }

    public Set<String> keySet() {
        return fileCache.keySet();
    }

    public void remove(String key) {
        fileCache.remove(key);
    }

    private void incrCacheFileCount() {
        Integer originValue = fileCountCache.getOrDefault(CACHE_FILE_COUNT_KEY, 0);
        fileCountCache.put(CACHE_FILE_COUNT_KEY, originValue + 1);
    }

    private void incrCacheDirectoryCount() {
        Integer originValue = fileCountCache.getOrDefault(CACHE_DIRECTORY_COUNT_KEY, 0);
        fileCountCache.put(CACHE_DIRECTORY_COUNT_KEY, originValue + 1);
    }

    public int getCacheFileCount() {
        return fileCountCache.getOrDefault(CACHE_FILE_COUNT_KEY, 0);
    }

    public int getCacheDirectorCount() {
        return fileCountCache.getOrDefault(CACHE_DIRECTORY_COUNT_KEY, 0);
    }

    public void updateConfig(SystemConfigDTO systemConfigCache) {
        this.systemConfigCache = systemConfigCache;
    }

    public SystemConfigDTO getConfig() {
        return this.systemConfigCache;
    }

    public void removeConfig() {
        this.systemConfigCache = null;
    }
}
