package im.zhaojun.zfile.core.config.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SaToken 权限配置, 配置管理员才能访问管理员功能.
 *
 * @author zhaojun
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    /**
     * 注册权限校验拦截器, 拦截所有 /admin/** 请求，但不包含 /admin 因为这个是登录页面.
     *
     * @param   registry
     *          拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/admin/**", () -> {
                StpUtil.checkLogin();
                StpUtil.checkRole("admin");
            });
        })).addPathPatterns("/**").excludePathPatterns("/admin");

        // 不再依赖 SaToken 的默认路径检查功能
        SaStrategy.instance.checkRequestPath = (path, extArg1, extArg2) -> {};
    }

}