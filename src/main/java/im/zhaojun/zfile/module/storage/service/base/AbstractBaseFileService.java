package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.util.ObjUtil;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.InitializeStorageSourceBizException;
import im.zhaojun.zfile.core.util.StrPool;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.share.context.ShareAccessContext;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.user.model.constant.UserConstant;
import im.zhaojun.zfile.module.user.model.entity.UserStorageSource;
import im.zhaojun.zfile.module.user.service.UserStorageSourceService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractBaseFileService<P extends IStorageParam> implements BaseFileService {

    @Resource
    private UserStorageSourceService userStorageSourceService;

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
            fileList(StringUtils.SLASH);
            isInitialized = true;
        } catch (Exception e) {
            throw new InitializeStorageSourceBizException(ErrorCode.BIZ_STORAGE_INIT_ERROR.getCode(), "初始化异常, 错误信息为: " + e.getMessage(), storageId, e);
        }
    }

    protected String getStorageSimpleInfo() {
        return String.format("存储源 [id=%s, name=%s, type: %s]", storageId, name, getStorageTypeEnum().getDescription());
    }


    public abstract StorageSourceMetadata getStorageSourceMetadata();

    public String getCurrentUserBasePath() {
        // 检查是否为分享访问，如果是则返回分享的基础路径
        if (ShareAccessContext.isShareAccess()) {
            return ShareAccessContext.getShareBasePath();
        }
        
        // 原有逻辑保持不变
        Integer userId = ZFileAuthUtil.getCurrentUserId();
        if (!this.isInitialized) {
            userId = UserConstant.ADMIN_ID;
        }
        UserStorageSource userStorageSource = userStorageSourceService.getByUserIdAndStorageId(userId, storageId);
        if (userStorageSource == null || StringUtils.isEmpty(userStorageSource.getRootPath())) {
            return StrPool.SLASH;
        } else {
            return userStorageSource.getRootPath();
        }
    }


    @Override
    public void destroy() {

    }
}