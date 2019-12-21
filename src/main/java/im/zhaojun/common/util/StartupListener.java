package im.zhaojun.common.util;

import im.zhaojun.common.exception.InitializeException;
import im.zhaojun.common.service.FileAsyncCacheService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 项目启动监听器, 当项目启动时, 遍历当前对象存储的所有内容, 添加到缓存中.
 * @author zhaojun
 */
@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        try {
            fileAsyncCacheService.cacheGlobalFile();
        } catch (Exception e) {
            throw new InitializeException("初始化缓存异常.", e);
        }
    }

}