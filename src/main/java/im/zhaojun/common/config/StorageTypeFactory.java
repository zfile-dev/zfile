package im.zhaojun.common.config;

import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.exception.UnknownStorageTypeException;
import im.zhaojun.common.service.FileService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 存储类型工厂类
 */
@Component
public class StorageTypeFactory implements ApplicationContextAware {

    private static Map<String, FileService> storageTypeEnumFileServiceMap;

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext act) throws BeansException {
        applicationContext = act;
        storageTypeEnumFileServiceMap = act.getBeansOfType(FileService.class);
    }

    public static FileService getTrafficMode(StorageTypeEnum type) {
        FileService result = null;
        for (FileService fileService : storageTypeEnumFileServiceMap.values()) {
            if (fileService.getStorageTypeEnum() == type) {
                result = fileService;
                break;
            }
        }

        if (result == null) {
            throw new UnknownStorageTypeException(type.getDescription());
        }
        return result;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}