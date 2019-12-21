package im.zhaojun.common.aspect;

import im.zhaojun.common.exception.StorageStrategyUninitializedException;
import im.zhaojun.common.model.enums.StorageTypeEnum;
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
        StorageTypeEnum currentStorageStrategy = systemConfigService.getCurrentStorageStrategy();

        if (currentStorageStrategy == null) {
            throw new StorageStrategyUninitializedException("存储策略未初始化");
        }
    }

}
