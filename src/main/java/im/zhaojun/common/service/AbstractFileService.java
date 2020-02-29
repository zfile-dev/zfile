package im.zhaojun.common.service;

import cn.hutool.core.util.BooleanUtil;
import im.zhaojun.common.cache.ZFileCache;
import im.zhaojun.common.model.constant.ZFileConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractFileService extends FileCacheService implements FileService {

    private static final String SYSTEM_CONFIG_CACHE_PREFIX = "zfile-cache:";

    @Value("${zfile.cache.timeout}")
    protected Long timeout;

    protected boolean isInitialized = false;

    protected String basePath;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    @Resource
    private ZFileCache zFileCache;

    /***
     * 获取指定路径下的文件及文件夹, 默认缓存 60 分钟，每隔 30 分钟刷新一次.
     * @param path 文件路径
     * @return     文件及文件夹列表
     * @throws Exception  获取文件列表中出现的异常
     */
    @Override
    public abstract List<FileItemDTO> fileList(String path) throws Exception;

    /**
     * 清理当前存储策略的缓存
     * 1. 删除全部缓存
     * 2. 标记为当前处于未完成缓存状态
     */
    public void clearFileCache() {
        zFileCache.clear();
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
        boolean searchIgnoreCase = systemConfigService.getSearchIgnoreCase();
        return zFileCache.find(name, searchIgnoreCase);
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
     * 刷新缓存
     */
    public void refreshCache(String key) throws Exception {
        zFileCache.remove(key);
        FileService currentFileService = (FileService) AopContext.currentProxy();
        currentFileService.fileList(key);
    }

    public abstract FileItemDTO getFileItem(String path);

}
