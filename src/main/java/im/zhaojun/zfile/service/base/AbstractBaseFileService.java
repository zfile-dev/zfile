package im.zhaojun.zfile.service.base;

import im.zhaojun.zfile.cache.ZFileCache;
import im.zhaojun.zfile.exception.InitializeDriveException;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractBaseFileService implements BaseFileService {


    @Resource
    private ZFileCache zFileCache;

    /**
     * 下载链接过期时间, 目前只在兼容 S3 协议的存储策略中使用到.
     */
    @Value("${zfile.cache.timeout}")
    protected Long timeout;

    /**
     * 是否初始化成功
     */
    protected boolean isInitialized = false;

    /**
     * 基路径
     */
    protected String basePath;

    /**
     * 驱动器 ID
     */
    public Integer driveId;

    /***
     * 获取指定路径下的文件及文件夹, 默认缓存 60 分钟，每隔 30 分钟刷新一次.
     *
     * @param   path
     *          文件路径
     *
     * @return  文件及文件夹列表
     *
     * @throws Exception  获取文件列表中出现的异常
     */
    @Override
    public abstract List<FileItemDTO> fileList(String path) throws Exception;


    /**
     * 清理当前存储策略的缓存
     */
    public void clearFileCache() {
        zFileCache.clear(driveId);
    }


    /**
     * 初始化方法, 启动时自动调用实现类的此方法进行初始化.
     *
     * @param   driveId
     *          驱动器 ID
     */
    public abstract void init(Integer driveId);


    /**
     * 测试是否连接成功, 会尝试取调用获取根路径的文件, 如果没有抛出异常, 则认为连接成功, 某些存储策略需要复写此方法.
     */
    protected void testConnection() {
        try {
            fileList("/");
        } catch (Exception e) {
            throw new InitializeDriveException("初始化异常, 错误信息为: " + e.getMessage(), e);
        }
    }


    /**
     * 获取是否初始化成功
     *
     * @return  初始化成功与否
     */
    public boolean getIsUnInitialized() {
        return !isInitialized;
    }


    /**
     * 获取是否初始化成功
     *
     * @return  初始化成功与否
     */
    public boolean getIsInitialized() {
        return isInitialized;
    }


    /**
     * 获取当前实现类的存储策略类型
     *
     * @return  存储策略类型枚举对象
     */
    public abstract StorageTypeEnum getStorageTypeEnum();


    /**
     * 获取初始化当前存储策略, 所需要的参数信息 (用于表单填写)
     *
     * @return  初始化所需的参数列表
     */
    public abstract List<StorageConfig> storageStrategyConfigList();


    /**
     * 搜索文件
     *
     * @param   name
     *          文件名
     *
     * @return  包含该文件名的所有文件或文件夹
     */
    public List<FileItemDTO> search(String name) {
        return zFileCache.find(driveId, name);
    }


    /**
     * 获取单个文件信息
     *
     * @param   path
     *          文件路径
     *
     * @return  单个文件的内容.
     */
    public abstract FileItemDTO getFileItem(String path);

    public Integer getDriveId() {
        return driveId;
    }

    public void setDriveId(Integer driveId) {
        this.driveId = driveId;
    }
}