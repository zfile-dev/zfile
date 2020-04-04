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
 * ZFile 缓存类
 */
@Component
public class ZFileCache {

    /**
     * 缓存 map 对象.
     * key: 文件夹路径
     * value: 文件夹中内容
     */
    private ConcurrentMap<String, List<FileItemDTO>> cache = new ConcurrentHashMap<>();

    /**
     * 系统设置缓存
     */
    private SystemConfigDTO systemConfigCache;

    /**
     * 缓存最后自动刷新时间
     */
    public Date lastCacheAutoRefreshDate;

    /**
     * 写入缓存
     * @param key       文件夹路径
     * @param value     文件夹中内容
     */
    public synchronized void put(String key, List<FileItemDTO> value) {
        cache.put(key, value);
    }

    /**
     * 根据文件夹路径取的环境
     * @param key       文件夹路径
     * @return          文件夹中内容
     */
    public List<FileItemDTO> get(String key) {
        return cache.get(key);
    }

    /**
     * 清空缓存.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 获取已缓存文件夹数量
     * @return  已缓存文件夹数量
     */
    public int cacheCount() {
        return cache.size();
    }

    /**
     * 搜索缓存中内容
     * @param key                           搜索键, 可匹配文件夹名称和文件名称.
     * @param ignoreCase                    是否忽略大小写, true 为忽略, false 为不忽略.
     * @param searchContainEncryptedFile    搜索是否包含加密文件. true 为不包含, false 为包含, 用于控制当文件夹被密码保护时, 是否出现在搜索结果中.
     * @return                              搜索结果, 包含文件夹和文件.
     */
    public List<FileItemDTO> find(String key, boolean ignoreCase, boolean searchContainEncryptedFile) {
        List<FileItemDTO> result = new ArrayList<>();

        Collection<List<FileItemDTO>> values = cache.values();
        for (List<FileItemDTO> fileItemList : values) {

            // 过滤加密文件
            if (!searchContainEncryptedFile && isEncryptedFolder(fileItemList)) {
                continue;
            }

            for (FileItemDTO fileItemDTO : fileItemList) {
                boolean testResult;

                // 根据是否需要忽略大小写来匹配文件(夹)名
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

    /**
     * 获取所有缓存 key (文件夹名称)
     * @return      所有缓存 key
     */
    public Set<String> keySet() {
        return cache.keySet();
    }

    /**
     * 从缓存中删除一个条目
     * @param key   文件夹名称
     */
    public void remove(String key) {
        cache.remove(key);
    }

    /**
     * 更新缓存中的系统设置
     * @param systemConfigCache 系统设置
     */
    public void updateConfig(SystemConfigDTO systemConfigCache) {
        this.systemConfigCache = systemConfigCache;
    }

    /**
     * 从获取中获取系统设置
     * @return  系统设置
     */
    public SystemConfigDTO getConfig() {
        return this.systemConfigCache;
    }

    /**
     * 清空系统设置缓存
     */
    public void removeConfig() {
        this.systemConfigCache = null;
    }

    /**
     * 获取缓存最后刷新时间
     * @return  缓存最后刷新时间
     */
    public Date getLastCacheAutoRefreshDate() {
        return lastCacheAutoRefreshDate;
    }

    /**
     * 更新缓存最后刷新时间
     * @param lastCacheAutoRefreshDate  缓存最后刷新时间
     */
    public void setLastCacheAutoRefreshDate(Date lastCacheAutoRefreshDate) {
        this.lastCacheAutoRefreshDate = lastCacheAutoRefreshDate;
    }


    /**
     * 判断是否为加密文件夹
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
