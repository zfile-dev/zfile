package im.zhaojun.zfile.model.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * @author zhaojun
 */
@Data
public class CacheConfigDTO {
    private Boolean enableCache;
    private Boolean cacheFinish;
    private Set<String> cacheKeys;
    private Integer cacheDirectoryCount;
    private Integer cacheFileCount;
    private Date lastCacheAutoRefreshDate;
}
