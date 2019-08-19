package im.zhaojun.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineConfiguration {

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    public static final String CACHE_NAME = "zfile";

    /**
     * 个性化配置缓存
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        ArrayList<CaffeineCache> caches = new ArrayList<>();
        caches.add(new CaffeineCache(CACHE_NAME,
                Caffeine.newBuilder().recordStats()
                        .expireAfterWrite(timeout, TimeUnit.SECONDS)
                        .build())
        );
        manager.setCaches(caches);
        return manager;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                char separator = ':';
                StringBuilder strBuilder = new StringBuilder();
                // 类名
                strBuilder.append(target.getClass().getSimpleName());
                strBuilder.append(separator);
                // 方法名
                strBuilder.append(method.getName());
                strBuilder.append(separator);
                // 参数值
                for (Object object : params) {
                    strBuilder.append(object).append(",");
                }
                return strBuilder.toString();
            }
        };
    }
}