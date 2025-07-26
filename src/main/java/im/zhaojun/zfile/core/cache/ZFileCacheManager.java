package im.zhaojun.zfile.core.cache;

import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * ZFile 业务缓存，针对无法使用 jsr-107 的缓存进行处理的业务逻辑。
 */
@Component
public class ZFileCacheManager {

    /**
     * 用户可用的存储源列表缓存
     */
    private static final Map<Integer, List<StorageSource>> userEnableStorageSourceCache = new ConcurrentHashMap<>();

    /**
     * 根据用户 ID 获取可用的存储源列表，若缓存中不存在，则通过 mappingFunction 获取并返回。
     *
     * @param   userId
     *          用户 ID
     *
     * @param   mappingFunction
     *          当缓存中不存在时，用于获取存储源列表的函数。
     *
     * @return  存储源列表函数
     */
    public List<StorageSource> findAllEnableOrderByOrderNum(Integer userId, Function<Integer, List<StorageSource>> mappingFunction) {
        return userEnableStorageSourceCache.computeIfAbsent(userId, mappingFunction);
    }

    /**
     * 清空所有用户的存储源缓存。
     */
    public void clearUserEnableStorageSourceCache() {
        userEnableStorageSourceCache.clear();
    }

    /**
     * 清除指定用户的存储源缓存。
     *
     * @param   userId
     *          用户 ID
     */
    public void clearUserEnableStorageSourceCache(Integer userId) {
        userEnableStorageSourceCache.remove(userId);
    }

}