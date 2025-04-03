package im.zhaojun.zfile.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 *
 * @author zhaojun
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLimit {

    /**
     * 持续时间
     */
    int timeout();

    /**
     * 时间单位, 默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 单位时间内允许访问的最大次数
     */
    long maxCount();

}