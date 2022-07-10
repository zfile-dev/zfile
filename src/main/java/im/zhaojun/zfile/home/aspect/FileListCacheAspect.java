package im.zhaojun.zfile.home.aspect;

import im.zhaojun.zfile.common.cache.ZFileCache;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 缓存切面类, 用于访问文件夹时, 缓存文件列表内容.
 *
 * @author zhaojun
 */
@Aspect
@Component
public class FileListCacheAspect {

    @Resource
    private ZFileCache zFileCache;

    @Resource
    private StorageSourceService storageSourceService;

    /**
     * 缓存切面, 如果此存储源开启了缓存, 则从缓存中取数据, 没有开启, 则直接调用方法.
     */
    @Around(value = "execution(public * im.zhaojun.zfile.home.service.base.AbstractBaseFileService.fileList(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        List<FileItemResult> result;

        // 获取请求路径
        Object[] args = point.getArgs();
        String path = String.valueOf(args[0]);

        // 获取当前存储源
        AbstractBaseFileService<?> fileService = ((AbstractBaseFileService) point.getTarget());
        Integer storageId = fileService.storageId;

        // 判断存储源是否开启了缓存
        StorageSource storageSource = storageSourceService.findById(storageId);
        boolean enableCache = storageSource.getEnableCache();

        if (enableCache) {
            List<FileItemResult> cacheFileList = zFileCache.get(storageId, path);
            if (cacheFileList == null) {
                result = Collections.unmodifiableList((List<FileItemResult>) point.proceed());
                zFileCache.put(storageId, path, result);
            } else {
                result = cacheFileList;
            }
        } else {
            result = (List<FileItemResult>) point.proceed();
        }

        return result;
    }

}