package im.zhaojun.zfile.core;

import im.zhaojun.zfile.exception.StorageStrategyUninitializedException;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.util.SpringContextHolder;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author zhaojun
 */
@Aspect
@Component
public class StorageStrategyInitCheckAop {

    @Before("@annotation(im.zhaojun.zfile.model.annotation.CheckStorageStrategyInit)")
    public void logStart() {
        SystemConfigService systemConfigService = SpringContextHolder.getBean(SystemConfigService.class);
        AbstractBaseFileService currentFileService = systemConfigService.getCurrentFileService();
        if (currentFileService == null) {
            throw new StorageStrategyUninitializedException("存储策略尚未初始化, 请联系管理员!");
        }
        if (currentFileService.getIsUnInitialized()) {
            throw new StorageStrategyUninitializedException("存储策略异常, 请联系管理员!");
        }

    }

}
