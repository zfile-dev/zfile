package im.zhaojun.zfile.cache;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import org.springframework.stereotype.Component;

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

    public List<FileItemDTO> find(String key, boolean ignoreCase, boolean searchContainEncryptedFile) {
        List<FileItemDTO> result = new ArrayList<>();

        Collection<List<FileItemDTO>> values = fileCache.values();
        for (List<FileItemDTO> fileItemList : values) {

            // 如果开启了 "搜索包含加密文件" 选项, 则直接返回 true.
            if (!searchContainEncryptedFile && isEncryptedFolder(fileItemList)) {
                continue;
            }

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


    /**
     * 不是加密文件夹
     * @param list      文件夹中的内容
     * @return          返回此文件夹是否加密.
     */
    private boolean isEncryptedFolder(List<FileItemDTO> list) {
        // 遍历文件判断是否包含
        for (FileItemDTO fileItemDTO : list) {
            if (Objects.equals(ZFileConstant.PASSWORD_FILE_NAME, fileItemDTO.getName())) {
                return true;
            }
        }
        return false;
    }
}
