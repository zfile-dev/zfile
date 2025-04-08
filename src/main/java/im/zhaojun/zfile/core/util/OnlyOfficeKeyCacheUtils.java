package im.zhaojun.zfile.core.util;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.CacheObj;
import im.zhaojun.zfile.module.onlyoffice.model.OnlyOfficeFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OnlyOffice 文件信息与 Key 缓存工具类
 *
 * @author zhaojun
 */
@Slf4j
public class OnlyOfficeKeyCacheUtils {

    /**
     * 存储 OnlyOffice 文件信息与 Key 的映射关系. 最多存储 10000 个 Key, 防止内存溢出.
     */
    private static final Cache<OnlyOfficeFile, String> ONLY_OFFICE_FILE_KEY_MAP = CacheUtil.newLRUCache(10000);

    /**
     * 存储 OnlyOffice Key 与文件信息的映射关系. 最多存储 10000 个 Key, 防止内存溢出.
     */
    private static final Cache<String, OnlyOfficeFile> ONLY_OFFICE_KEY_FILE_MAP = CacheUtil.newLRUCache(10000);

    /**
     * 存储文件锁, 防止并发操作文件缓存时出现问题.
     */
    private static final Cache<OnlyOfficeFile, ReentrantLock> locks = CacheUtil.newLRUCache(300);

    /**
     * 获取该文件缓存的 key, 如果不存在则生成一个新的 key 并缓存.
     *
     * @param   onlyOfficeFile
     *          OnlyOffice 文件信息
     *
     * @return  该文件唯一标识
     */
    public static String getKeyOrPutNew(OnlyOfficeFile onlyOfficeFile, long timeout) {
        ReentrantLock lock = getLock(onlyOfficeFile);
        try {
            boolean getLock = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
            if (BooleanUtils.isFalse(getLock)) {
                log.warn("{} 尝试获取锁超时, 强制忽略锁直接操作文件.", onlyOfficeFile);
            }
            try {
                if (ONLY_OFFICE_FILE_KEY_MAP.containsKey(onlyOfficeFile)) {
                    return ONLY_OFFICE_FILE_KEY_MAP.get(onlyOfficeFile);
                } else {
                    String key = RandomStringUtils.randomAlphabetic(10);
                    ONLY_OFFICE_FILE_KEY_MAP.put(onlyOfficeFile, key);
                    ONLY_OFFICE_KEY_FILE_MAP.put(key, onlyOfficeFile);
                    return key;
                }
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread was interrupted", e);
        }

    }

    /**
     * 清理缓存中的 Key 与文件信息的映射关系.(文件发生了变化, 需要重新生成 OnlyOffice 预览链接时调用)
     *
     * @param   key
     *          文件唯一标识
     */
    public static OnlyOfficeFile removeByKey(String key) {
        OnlyOfficeFile onlyOfficeFile = ONLY_OFFICE_KEY_FILE_MAP.get(key);
        if (onlyOfficeFile == null) {
            return null;
        }
        ONLY_OFFICE_FILE_KEY_MAP.remove(onlyOfficeFile);
        ONLY_OFFICE_KEY_FILE_MAP.remove(key);
        return onlyOfficeFile;
    }

    /**
     * 清理缓存中的文件信息与 Key 的映射关系.(文件发生了变化, 需要重新生成 OnlyOffice 预览链接时调用)
     *
     * @param   onlyOfficeFile
     *          OnlyOffice 文件信息
     */
    public static OnlyOfficeFile removeByFile(OnlyOfficeFile onlyOfficeFile) {
        String key = ONLY_OFFICE_FILE_KEY_MAP.get(onlyOfficeFile);
        if (key == null) {
            return null;
        }
        ONLY_OFFICE_FILE_KEY_MAP.remove(onlyOfficeFile);
        ONLY_OFFICE_KEY_FILE_MAP.remove(key);
        return onlyOfficeFile;
    }


    /**
     * 清理缓存中的某个文件夹下所有文件信息与 Key 的映射关系.(文件发生了变化, 需要重新生成 OnlyOffice 预览链接时调用)
     *
     * @param   onlyOfficeFile
     *          OnlyOffice 文件信息
     */
    public static List<OnlyOfficeFile> removeByFolder(OnlyOfficeFile onlyOfficeFile) {
        List<OnlyOfficeFile> caches = new ArrayList<>();
        Iterator<CacheObj<OnlyOfficeFile, String>> cacheObjIterator = ONLY_OFFICE_FILE_KEY_MAP.cacheObjIterator();
        while (cacheObjIterator.hasNext()) {
            CacheObj<OnlyOfficeFile, String> cacheObj = cacheObjIterator.next();
            OnlyOfficeFile cacheOnlyOfficeFile = cacheObj.getKey();
            if (cacheOnlyOfficeFile.getStorageKey().equals(onlyOfficeFile.getStorageKey())
                    && StringUtils.startWith(cacheOnlyOfficeFile.getPathAndName(), onlyOfficeFile.getPathAndName())) {
                ONLY_OFFICE_FILE_KEY_MAP.remove(cacheObj.getKey());
                ONLY_OFFICE_KEY_FILE_MAP.remove(cacheObj.getValue());
                caches.add(cacheOnlyOfficeFile);
            }
        }
        return caches;
    }

    /**
     * 获取文件锁, 防止并发操作文件缓存时出现问题.
     *
     * @param   key
     *          文件唯一标识
     *
     * @return  锁对象
     */
    public static ReentrantLock getLock(OnlyOfficeFile key) {
        return locks.get(key, true, ReentrantLock::new);
    }

}