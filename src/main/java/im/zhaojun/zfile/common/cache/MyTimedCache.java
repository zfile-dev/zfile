package im.zhaojun.zfile.common.cache;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.admin.model.dto.StorageSourceCacheKey;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义缓存类, 实现缓存超时后自动刷新
 *
 * @author zhaojun
 */
@Slf4j
public class MyTimedCache<K, V> extends TimedCache<K, V> {

    private StorageSourceContext storageSourceContext;

    public MyTimedCache(long timeout) {
        super(timeout);
    }


    /**
     * 当缓存超时后自动刷新
     *
     * @param   key
     *          缓存 key
     *
     * @param   cachedObject
     *          缓存值
     */
    @Override
    protected void onRemove(K key, V cachedObject) {
        if (storageSourceContext == null) {
            storageSourceContext = SpringUtil.getBean(StorageSourceContext.class);
        }

        StorageSourceCacheKey cacheKey = (StorageSourceCacheKey) key;
        AbstractBaseFileService<?> baseFileService = storageSourceContext.get(cacheKey.getStorageId());

        if (log.isDebugEnabled()) {
            log.debug("尝试刷新缓存: {}", cacheKey);
        }

        if (baseFileService == null) {
            log.error("尝试刷新缓存: {}, 时出现异常, 存储源已不存在", cacheKey);
            return;
        }

        try {
            baseFileService.fileList(cacheKey.getKey());
        } catch (Exception e) {
            log.error("尝试刷新缓存 {} 失败", cacheKey);
            e.printStackTrace();
        }

    }

}