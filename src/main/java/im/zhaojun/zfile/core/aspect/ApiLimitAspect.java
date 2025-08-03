package im.zhaojun.zfile.core.aspect;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.extra.servlet.JakartaServletUtil;
import im.zhaojun.zfile.core.annotation.ApiLimit;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.RequestHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 接口限流切面, 通过注解 {@link ApiLimit} 进行限流.
 *
 * @author zhaojun
 */
@Aspect
@Component
public class ApiLimitAspect {

    private final TimedCache<String, AtomicLong> apiLimitTimedCache = CacheUtil.newTimedCache(1000);

    public static final String API_LIMIT_KEY_PREFIX = "api_limit_";

    /**
     * 在标记了 {@link ApiLimit} 注解的方法执行前进行限流校验.
     *
     * @param joinPoint 切点
     */
    @Before("@annotation(apiLimit)")
    public void before(JoinPoint joinPoint, ApiLimit apiLimit) {
        // 获取当前请求的方法上的注解中设置的值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 反射获取当前被调用的方法
        Method method = signature.getMethod();
        int timeout = apiLimit.timeout();
        TimeUnit timeUnit = apiLimit.timeUnit();
        long millis = timeUnit.toMillis(timeout);
        long maxCount = apiLimit.maxCount();

        // 获取请求相关信息
        String ip = JakartaServletUtil.getClientIP(RequestHolder.getRequest());

        // 限制访问次数
        String key = API_LIMIT_KEY_PREFIX.concat(ip).concat(method.getName());
        AtomicLong atomicLong = apiLimitTimedCache.get(key, false);
        if (atomicLong == null) {
            apiLimitTimedCache.put(key, new AtomicLong(1), millis);
        } else {
            if (atomicLong.incrementAndGet() > maxCount) {
                throw new BizException(ErrorCode.BIZ_ACCESS_TOO_FREQUENT);
            }
        }
    }

}