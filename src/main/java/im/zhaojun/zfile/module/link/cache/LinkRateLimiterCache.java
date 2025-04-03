package im.zhaojun.zfile.module.link.cache;

import cn.hutool.cache.impl.CacheObj;
import cn.hutool.cache.impl.TimedCache;
import im.zhaojun.zfile.module.link.model.dto.CacheInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 直链/短链访问频率限制缓存
 */
@Service
public class LinkRateLimiterCache {

    /**
     * cache 在 put 时不指定 timeout, 则使用默认的 timeout. (单位: 毫秒)
     */
    public static final Integer DEFAULT_TIME_OUT = 60_000;

    private final TimedCache<String, AtomicInteger> timedCache = new TimedCache<>(DEFAULT_TIME_OUT);

    public boolean containsKey(String key) {
        return timedCache.containsKey(key);
    }

    public AtomicInteger get(String key, boolean isUpdateLastAccess) {
        return timedCache.get(key, isUpdateLastAccess);
    }

    public void put(String key, AtomicInteger object, long timeout) {
        timedCache.put(key, object, timeout);
    }

    public List<CacheInfo<String, AtomicInteger>> getCacheInfo() {
        List<CacheInfo<String, AtomicInteger>> cacheInfoList = new ArrayList<>();

        Iterator<CacheObj<String, AtomicInteger>> cacheObjIterator = timedCache.cacheObjIterator();
        while (cacheObjIterator.hasNext()) {
            CacheObj<String, AtomicInteger> next = cacheObjIterator.next();
            CacheInfo<String, AtomicInteger> cacheInfo = new CacheInfo<>();
            cacheInfo.setKey(next.getKey());
            cacheInfo.setValue(next.getValue());
            cacheInfo.setTtl(next.getTtl());
            cacheInfo.setExpiredTime(next.getExpiredTime());
            cacheInfoList.add(cacheInfo);
        }

        return cacheInfoList;
    }

}