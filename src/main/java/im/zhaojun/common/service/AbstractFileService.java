package im.zhaojun.common.service;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.RefreshPolicy;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import im.zhaojun.common.model.constant.ZFileConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.SystemConfigDTO;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhaojun
 * @date 2019/12/28 19:27
 */
@Slf4j
public abstract class AbstractFileService extends FileCacheService implements FileService {

    private static final String SYSTEM_CONFIG_CACHE_PREFIX = "zfile-cache:";

    @Value("${zfile.cache.timeout}")
    protected Long timeout;

    protected boolean isInitialized = false;

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
     * 1. 删除全部缓存
     * 2. 关闭自动刷新
     * 3. 重置缓存个数
     * 4. 标记为当前处于未完成缓存状态
     */
    public void clearFileCache() throws Exception {
        Set<String> cacheKeys = getCacheKeys();
        cache.removeAll(cacheKeys);
        closeCacheAutoRefresh();
        fileAsyncCacheService.resetCacheCount();
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
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常", e);
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
     * 获取是否初始化成功
     * @return              初始化成功与否
     */
    public boolean getIsInitialized() {
        return isInitialized;
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
     */
    public List<FileItemDTO> search(String name) {
        List<FileItemDTO> result = new ArrayList<>();

        boolean searchIgnoreCase = systemConfigService.getSearchIgnoreCase();

        List<FileItemDTO> fileItemList = selectAllFileList();
        for (FileItemDTO fileItemDTO : fileItemList) {

            boolean testResult;

            if (searchIgnoreCase) {
                testResult = StrUtil.containsIgnoreCase(fileItemDTO.getName(), name);
            } else {
                testResult = fileItemDTO.getName().contains(name);
            }

            if (testResult) {
                result.add(fileItemDTO);
            }
        }

        return result;
    }

    /**
     * 查询所有文件, 仅去缓存中查询.
     * @return              所有文件
     */
    public List<FileItemDTO> selectAllFileList() {
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
                if (cacheList != null && isNotEncryptedFolder(cacheList)) {
                    queue.addAll(cacheList);
                }
            }

        }
        return result;
    }

    /**
     * 不是加密文件夹
     * @param list      文件夹中的内容
     * @return          返回此文件夹是否加密.
     */
    private boolean isNotEncryptedFolder(List<FileItemDTO> list) {
        // 如果开启了 "搜索包含加密文件" 选项, 则直接返回 true.
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        if (BooleanUtil.isFalse(systemConfig.getSearchContainEncryptedFile())) {
            return true;
        }

        // 遍历文件判断是否包含
        for (FileItemDTO fileItemDTO : list) {
            if (Objects.equals(ZFileConstant.PASSWORD_FILE_NAME, fileItemDTO.getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取所有缓存的 Key, 仅当开启缓存, 且缓存完成时, 可获取.
     * @return              所有缓存的 Key
     */
    public Set<String> getCacheKeys() {
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

    public abstract FileItemDTO getFileItem(String path);

}
