package im.zhaojun.common.util;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.ViewConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.ViewConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 项目启动监听器, 当项目启动时, 遍历当前对象存储的所有内容, 添加到缓存中.
 */
@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);
    @Resource
    private ViewConfigService viewConfigService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ViewConfig viewConfig = viewConfigService.getViewConfig();
        StorageTypeEnum storageStrategy = viewConfig.getStorageStrategy();
        FileService fileService = StorageTypeFactory.getStorageTypeService(storageStrategy);
        new Thread(() -> {
            log.info("缓存 {} 所有文件开始", storageStrategy.getDescription());
            long startTime = System.currentTimeMillis();
            try {
                fileService.selectAllFileList();
            } catch (Exception e) {
                log.error("缓存所有文件失败", e);
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            log.info("缓存 {} 所有文件结束, 用时: {} 秒", storageStrategy.getDescription(), ( (endTime - startTime) / 1000 ));
        }).start();
    }

}