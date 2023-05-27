package im.zhaojun.zfile.module.storage.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 直链访问频率限制注解，标注了此注解的请求，会被校验访问频率是否符合要求.
 *
 * @author zhaojun
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LinkRateLimiter {

}