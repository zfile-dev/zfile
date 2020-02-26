package im.zhaojun.common.config;

import im.zhaojun.common.model.dto.FileItemDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhaojun
 */
@Configuration
public class CacheConfig {

    @Bean
    public ConcurrentMap<String, List<FileItemDTO>> concurrentMapCache() {
        return new ConcurrentHashMap<>();
    }
}
