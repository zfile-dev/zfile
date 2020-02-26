package im.zhaojun.common.aop;

import im.zhaojun.common.cache.ZFileCache;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.service.SystemConfigService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 操作日志切面.
 */
@Aspect
@Component
public class FileListCacheAspect {

    @Resource
    private ZFileCache zFileCache;

    @Resource
    private SystemConfigService systemConfigService;

    @Pointcut("execution(public * im.zhaojun.common.service.AbstractFileService.fileList(..))")
    public void pointcut() {
    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        List<FileItemDTO> result;

        Object[] args = point.getArgs();
        String path = String.valueOf(args[0]);

        boolean enableCache = systemConfigService.getEnableCache();

        if (enableCache) {
            List<FileItemDTO> cacheFileList = zFileCache.get(path);
            if (CollectionUtils.isEmpty(cacheFileList)) {
                result = (List<FileItemDTO>) point.proceed();
                zFileCache.put(path, result);
            } else {
                result = cacheFileList;
            }
        } else {
            result = (List<FileItemDTO>) point.proceed();
        }
        return result;
    }
}