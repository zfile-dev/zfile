package im.zhaojun.common.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhaojun
 * @date 2019/12/28 19:27
 */
@Slf4j
public abstract class AbstractFileService extends FileCacheService implements FileService {

    public static final String SYSTEM_CONFIG_CACHE_PREFIX = "zfile-cache:";

    @Value("${zfile.cache.timeout}")
    protected Long timeout;

    protected boolean isInitialized;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    @CreateCache(name = SYSTEM_CONFIG_CACHE_PREFIX, cacheType = CacheType.LOCAL)
    private Cache<String, List<FileItemDTO>> cache;

    /***
     * 获取指定路径下的文件及文件夹, 默认缓存 60 分钟，每隔 30 分钟刷新一次.
     * @param path 文件路径
     * @return     文件及文件夹列表
     * @throws Exception  获取文件列表中出现的异常
     */
    @Override
    @Cached(name = SYSTEM_CONFIG_CACHE_PREFIX,
            key =  "args[0]",
            cacheType = CacheType.LOCAL, localLimit = 100000, condition = "mvel{bean('systemConfigService').enableCache}")
    @CacheRefresh(refresh = 30, timeUnit = TimeUnit.MINUTES)
    public abstract List<FileItemDTO> fileList(String path) throws Exception;

    /**
     * 清理当前存储策略的缓存
     */
    public void clearFileCache() throws Exception {
        Set<String> cacheKeys = getCacheKeys();
        cache.removeAll(cacheKeys);
        closeCacheAutoRefresh();
        fileAsyncCacheService.setCacheFinish(false);
    }

    /**
     * 初始化方法, 启动时自动调用实现类的此方法进行初始化.
     */
    @PostConstruct
    public abstract void init();

    protected boolean testConnection() {
        boolean flag = true;
        try {
            fileList("/");
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + "初始化异常", e);
            flag = false;
        }
        return flag;
    }

    /**
     * 获取是否初始化成功
     * @return              初始化成功与否
     */
    public boolean getIsUnInitialized() {
        return !isInitialized;
    }

    /**
     * 获取存储策略类型
     * @return              存储策略类型枚举
     */
    public abstract StorageTypeEnum getStorageTypeEnum();

    /**
     * 搜索文件
     * @param name          文件名
     * @return              包含该文件名的所有文件或文件夹
     * @throws Exception    搜索过程出现的异常
     */
    public List<FileItemDTO> search(String name) throws Exception {
        List<FileItemDTO> result = new ArrayList<>();

        List<FileItemDTO> fileItemList = selectAllFileList();
        for (FileItemDTO fileItemDTO : fileItemList) {
            if (fileItemDTO.getName().contains(name)) {
                result.add(fileItemDTO);
            }
        }

        return result;
    }

    /**
     * 查询所有文件, 仅去缓存中查询.
     * @return              所有文件
     */
    public List<FileItemDTO> selectAllFileList() throws Exception {
        List<FileItemDTO> result = new ArrayList<>();
        boolean enableCache = systemConfigService.getEnableCache();
        if (!enableCache) {
            log.debug("未开启缓存, 不支持查询所有文件.");
            return null;
        }
        String path = "/";

        List<FileItemDTO> fileItemList = cache.get(path);
        fileItemList = fileItemList == null ? new ArrayList<>() : fileItemList;
        ArrayDeque<FileItemDTO> queue = new ArrayDeque<>(fileItemList);

        while (!queue.isEmpty()) {
            FileItemDTO fileItemDTO = queue.pop();
            result.add(fileItemDTO);
            if (fileItemDTO.getType() == FileTypeEnum.FOLDER) {
                String filePath = StringUtils.removeDuplicateSeparator("/" + fileItemDTO.getPath() + "/" + fileItemDTO.getName() + "/");
                List<FileItemDTO> cacheList = cache.get(filePath);
                if (cacheList != null) {
                    queue.addAll(cacheList);
                }
            }

        }
        return result;
    }

    /**
     * 获取所有缓存的 Key, 仅当开启缓存, 且缓存完成时, 可获取.
     * @return              所有缓存的 Key
     * @throws Exception    可能出现的异常
     */
    public Set<String> getCacheKeys() throws Exception {
        if (systemConfigService.getEnableCache() && fileAsyncCacheService.isCacheFinish()) {
            Set<String> collect = selectAllFileList().stream().map(fileItemDTO -> {
                if (fileItemDTO.getType() == FileTypeEnum.FOLDER) {
                    return StringUtils.removeDuplicateSeparator("/" + fileItemDTO.getPath() + "/" + fileItemDTO.getName() + "/");
                }
                return null;
            }).collect(Collectors.toSet());
            collect.remove(null);
            collect.add("/");
            return collect;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * 刷新缓存
     */
    public void refreshCache(String key) throws Exception {
        cache.remove(key);
        FileService currentFileService = (FileService) AopContext.currentProxy();
        currentFileService.fileList(key);
    }

    public void closeCacheAutoRefresh() {
        cache.config().setRefreshPolicy(null);
    }

    public void openCacheAutoRefresh() {
        RefreshPolicy refreshPolicy = RefreshPolicy.newPolicy(30, TimeUnit.MINUTES);
        cache.config().setRefreshPolicy(refreshPolicy);
    }

}
