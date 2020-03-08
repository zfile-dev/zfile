package im.zhaojun.zfile.core;

import im.zhaojun.zfile.cache.ZFileCache;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.service.SystemConfigService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaojun
 * 缓存切面类, 用于访问文件夹时, 缓存文件列表内容.
 */
@Aspect
@Component
public class FileListCacheAop {

    @Resource
    private ZFileCache zFileCache;

    @Resource
    private SystemConfigService systemConfigService;

    @Pointcut("execution(public * im.zhaojun.zfile.service.base.AbstractBaseFileService.fileList(..))")
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