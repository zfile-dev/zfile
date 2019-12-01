package im.zhaojun.common.util;

import im.zhaojun.common.service.FileAsyncCacheService;
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
    private FileAsyncCacheService fileAsyncCacheService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            fileAsyncCacheService.cacheGlobalFile();
        } catch (Exception e) {
            throw new RuntimeException("缓存异常.", e);
        }
    }

}