package im.zhaojun.common.model.dto;

import lombok.Data;

import java.util.Set;

/**
 * @author zhaojun
 * @date 2020/1/3 12:39
 */
@Data
public class CacheConfigDTO {
    private boolean enableCache;
    private boolean cacheFinish;
    private Set<String> cacheKeys;
    private Integer cacheDirectoryCount;
    private Integer cacheFileCount;
}
