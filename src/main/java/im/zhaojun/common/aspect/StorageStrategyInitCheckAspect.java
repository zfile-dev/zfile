package im.zhaojun.common.aspect;

import im.zhaojun.common.exception.StorageStrategyUninitializedException;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.SystemConfigService;
import im.zhaojun.common.util.SpringContextHolder;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author zhaojun
 */
@Aspect
@Component
public class StorageStrategyInitCheckAspect {

    @Before("@annotation(im.zhaojun.common.annotation.CheckStorageStrategyInit)")
    public void logStart() {
        SystemConfigService systemConfigService = SpringContextHolder.getBean(SystemConfigService.class);
        AbstractFileService currentFileService = systemConfigService.getCurrentFileService();
        if (currentFileService == null || !currentFileService.getIsInitialized()) {
            throw new StorageStrategyUninitializedException("存储策略异常, 请联系管理员!");
        }
    }

}
