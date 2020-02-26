package im.zhaojun.common.util;

import cn.hutool.core.net.NetUtil;
import im.zhaojun.common.cache.ZFileCache;
import im.zhaojun.common.exception.InitializeException;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileAsyncCacheService;
import im.zhaojun.common.service.SystemConfigService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 项目启动监听器, 当项目启动时, 遍历当前对象存储的所有内容, 添加到缓存中.
 * @author zhaojun
 */
@Component
@Slf4j
public class StartupListener implements ApplicationListener<ApplicationStartedEvent> {

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    @Resource
    private Environment environment;

    @Resource
    private ZFileCache zFileCache;

    @Value("${zfile.cache.auto-refresh.enable}")
    protected boolean enableAutoRefreshCache;

    @Value("${zfile.cache.auto-refresh.delay}")
    protected Long delay;

    @Value("${zfile.cache.auto-refresh.interval}")
    protected Long interval;

    @Resource
    private SystemConfigService systemConfigService;

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        printStartInfo();
        cacheAllFile();
        enableCacheAutoRefreshTask();
    }

    private void enableCacheAutoRefreshTask() {
        if (enableAutoRefreshCache) {
            new Timer("testTimer").schedule(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    boolean enableCache = systemConfigService.getEnableCache();

                    if (!enableCache) {
                        return;
                    }

                    log.debug("开始调用自动刷新缓存");

                    Set<String> keySet = zFileCache.keySet();
                    for (String key : keySet) {
                        zFileCache.remove(key);
                        AbstractFileService currentFileService = systemConfigService.getCurrentFileService();
                        currentFileService.fileList(key);
                    }
                }
            }, delay * 1000,interval * 1000);
        }
    }

    private void printStartInfo() {
        String serverPort = environment.getProperty("server.port", "8080");

        LinkedHashSet<String> localIps = NetUtil.localIps();
        StringBuilder indexAddr = new StringBuilder();
        StringBuilder indexAdminAddr = new StringBuilder();
        for (String localIp : localIps) {
            String addr = String.format("http://%s:%s", localIp, serverPort);
            indexAddr.append(addr).append("\t");
            indexAdminAddr.append(addr).append("/#/admin").append("\t");
        }
        log.info("ZFile started at          " + indexAddr);
        log.info("ZFile Admin started at    " + indexAdminAddr);
    }

    private void cacheAllFile() {
        try {
            fileAsyncCacheService.cacheGlobalFile();
        } catch (Exception e) {
            throw new InitializeException("初始化缓存异常.", e);
        }
    }
}