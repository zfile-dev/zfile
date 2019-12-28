package im.zhaojun.common.config;

import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zhaojun
 */
@Component
public class StorageTypeFactory implements ApplicationContextAware {

    private static Map<String, AbstractFileService> storageTypeEnumFileServiceMap;

    private static ApplicationContext applicationContext;

    /**
     * 项目启动时执行
     */
    @Override
    public void setApplicationContext(ApplicationContext act) throws BeansException {
        applicationContext = act;

        // 获取 Spring 容器中所有 FileService 类型的类
        storageTypeEnumFileServiceMap = act.getBeansOfType(AbstractFileService.class);
    }

    /**
     * 获取指定存储类型 Service
     */
    public static AbstractFileService getStorageTypeService(StorageTypeEnum type) {
        AbstractFileService result = null;
        for (AbstractFileService fileService : storageTypeEnumFileServiceMap.values()) {
            if (fileService.getStorageTypeEnum() == type) {
                result = fileService;
                break;
            }
        }
        return result;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}