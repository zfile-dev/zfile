package im.zhaojun.common.cache;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.service.SystemConfigService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhaojun
 */
@Component
public class ZFileCache {

    private ConcurrentMap<String, List<FileItemDTO>> fileCache = new ConcurrentHashMap<>();

    private SystemConfigDTO systemConfigCache;

    public Date lastCacheAutoRefreshDate;

    @Resource
    private SystemConfigService systemConfigService;

    public synchronized void put(String key, List<FileItemDTO> value) {
        fileCache.put(key, value);
    }

    public List<FileItemDTO> get(String key) {
        return fileCache.get(key);
    }

    public void clear() {
        fileCache.clear();
    }

    public int cacheCount() {
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

    public void updateConfig(SystemConfigDTO systemConfigCache) {
        this.systemConfigCache = systemConfigCache;
    }

    public SystemConfigDTO getConfig() {
        return this.systemConfigCache;
    }

    public void removeConfig() {
        this.systemConfigCache = null;
    }

    public Date getLastCacheAutoRefreshDate() {
        return lastCacheAutoRefreshDate;
    }

    public void setLastCacheAutoRefreshDate(Date lastCacheAutoRefreshDate) {
        this.lastCacheAutoRefreshDate = lastCacheAutoRefreshDate;
    }
}
