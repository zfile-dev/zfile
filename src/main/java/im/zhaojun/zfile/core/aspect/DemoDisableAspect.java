package im.zhaojun.zfile.core.aspect;

import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 通过注解 {@link DemoDisable} 限制演示系统不可操作的功能.
 *
 * @author zhaojun
 */
@Aspect
@Component
public class DemoDisableAspect {

    @Resource
    private ZFileProperties zFileProperties;

    /**
     * 定义一个切点（通过注解）
     */
    @Pointcut("@annotation(im.zhaojun.zfile.core.annotation.DemoDisable)")
    public void demoDisable() {
    }

    /**
     * 在标记了 {@link DemoDisable} 注解的方法执行前进行限流校验.
     *
     * @param joinPoint 切点
     */
    @Before("demoDisable()")
    public void before(JoinPoint joinPoint) {
        if (zFileProperties.isDemoSite()) {
            throw new BizException(ErrorCode.DEMO_SITE_DISABLE_OPERATOR);
        }
    }

}