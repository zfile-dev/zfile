package im.zhaojun.zfile.core.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache 相关配置
 *
 * @author zhaojun
 */
@Configuration
@EnableCaching
public class SpringCacheConfig {
	
	/**
	 * 使用 TransactionAwareCacheManagerProxy 装饰 ConcurrentMapCacheManager，使其支持事务 （将 put、evict、clear 操作延迟到事务成功提交再执行.）
	 */
	@Bean
	public CacheManager cacheManager() {
		return new TransactionAwareCacheManagerProxy(new ConcurrentMapCacheManager());
	}
	
}