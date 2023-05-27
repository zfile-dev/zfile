package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.exception.file.init.InitializeStorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractBaseFileService<P extends IStorageParam> implements BaseFileService {

    /**
     * 存储源初始化配置
     */
    @Getter
    public P param;

    /**
     * 是否初始化成功
     */
    @Getter
    protected boolean isInitialized = false;

    /**
     * 存储源 ID
     */
    @Getter
    public Integer storageId;

    /**
     * 存储源名称
     */
    @Getter
    private String name;

    public void init(String name, Integer storageId, P param) {
        if (!ObjUtil.hasNull(this.name, this.storageId, this.param)) {
            throw new IllegalStateException("请勿重复初始化");
        }
        if (ObjUtil.hasEmpty(name, storageId, param)) {
            throw new IllegalStateException("初始化参数不能为空");
        }
        this.name = name;
        this.storageId = storageId;
        this.param = param;
        init();
    }

    /**
     * 初始化存储源, 在调用前要设置存储的 {@link #storageId} 属性. 和 {@link #param} 属性.
     */
    public abstract void init();

    /**
     * 测试是否连接成功, 会尝试取调用获取根路径的文件, 如果没有抛出异常, 则认为连接成功.
     */
    public void testConnection() {
        try {
            fileList("/");
            isInitialized = true;
        } catch (Exception e) {
            throw new InitializeStorageSourceException(CodeMsg.STORAGE_SOURCE_INIT_FAIL, storageId, "初始化异常, 错误信息为: " + e.getMessage(), e).setResponseExceptionMessage(true);
        }
    }

    String getStorageSimpleInfo() {
        return StrUtil.format("存储源 [id={}, name={}, type: {}]", storageId, name, getStorageTypeEnum().getDescription());
    }

    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        return false;
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        return false;
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        return false;
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        return false;
    }

}