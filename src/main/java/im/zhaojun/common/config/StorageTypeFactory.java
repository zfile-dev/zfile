package im.zhaojun.common.config;

import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.FileService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StorageTypeFactory implements ApplicationContextAware {

    private static Map<String, FileService> storageTypeEnumFileServiceMap;

    private static ApplicationContext applicationContext;

    /**
     * 项目启动时执行
     */
    @Override
    public void setApplicationContext(ApplicationContext act) throws BeansException {
        applicationContext = act;

        // 获取 Spring 容器中所有 FileService 类型的类
        storageTypeEnumFileServiceMap = act.getBeansOfType(FileService.class);
    }

    /**
     * 获取指定存储类型 Service
     */
    public static FileService getStorageTypeService(StorageTypeEnum type) {
        FileService result = null;
        for (FileService fileService : storageTypeEnumFileServiceMap.values()) {
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