package im.zhaojun.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Collections;

/**
 * 缓存配置类, 用于根据配置决定使用 redis 缓存还是 caffeine (内存).
 */
@Configuration
public class ZFileCacheConfiguration {

    public static final String CACHE_NAME = "zfile";

    /**
     * 个性化配置缓存
     */
    @Bean
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "caffeine")
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCacheNames(Collections.singletonList(CACHE_NAME));
        return caffeineCacheManager;
    }


    @Bean
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        GenericJackson2JsonRedisSerializer jsonRedisSerializer
                = new GenericJackson2JsonRedisSerializer();
        RedisSerializationContext.SerializationPair<Object> pair
                = RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer);
        RedisCacheConfiguration defaultCacheConfig=RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
        return new RedisCacheManager(redisCacheWriter, defaultCacheConfig);
    }


    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            char separator = ':';
            StringBuilder strBuilder = new StringBuilder();

            // 类名
            strBuilder.append(target.getClass().getSimpleName());
            strBuilder.append(separator);

            // 方法名
            strBuilder.append(method.getName());
            strBuilder.append(separator);

            // 参数值
            for (int i = 0; i < params.length; i++) {
                if (i == params.length - 1) {
                    strBuilder.append(params[i]);
                } else {
                    strBuilder.append(params[i]).append(",");
                }
            }
            return strBuilder.toString();
        };
    }
}