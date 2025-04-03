package im.zhaojun.zfile.module.user.aspect;

import cn.hutool.extra.servlet.JakartaServletUtil;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.log.model.entity.LoginLog;
import im.zhaojun.zfile.module.log.service.LoginLogService;
import im.zhaojun.zfile.module.user.model.enums.LoginLogModeEnum;
import im.zhaojun.zfile.module.user.model.request.UserLoginRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
@Slf4j
public class LoginLogAspect {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private LoginLogService loginLogService;

    @Resource
    private HttpServletRequest httpServletRequest;

    public static final String DEFAULT_LOGIN_SUCCESS_RESULT = "登录成功";

    /**
     * 登录日志切面，拦截 im.zhaojun.zfile.module.user.controller.UserController.doLogin() 方法
     */
    @Around(value = "execution(* im.zhaojun.zfile.module.user.controller.UserController.doLogin(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        LoginLogModeEnum loginLogMode = systemConfig.getLoginLogMode();

        if (loginLogMode == LoginLogModeEnum.OFF) {
            return pjp.proceed();
        }

        // 获取方法的第一个参数 UserLoginRequest 对象
        Object[] args = pjp.getArgs();
        Object arg = args[0];
        UserLoginRequest userLoginRequest = (UserLoginRequest) arg;

        LoginLog loginLog = new LoginLog();
        loginLog.setUsername(userLoginRequest.getUsername());
        loginLog.setPassword(userLoginRequest.getPassword());
        loginLog.setCreateTime(new Date());
        loginLog.setIp(JakartaServletUtil.getClientIP(httpServletRequest));
        loginLog.setUserAgent(httpServletRequest.getHeader(HttpHeaders.USER_AGENT));
        loginLog.setReferer(httpServletRequest.getHeader(HttpHeaders.REFERER));

        String msg = DEFAULT_LOGIN_SUCCESS_RESULT;
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            msg = throwable.getMessage();
            throw throwable;
        } finally {
            if (loginLogMode != LoginLogModeEnum.ALL) {
                if (loginLogMode == LoginLogModeEnum.IGNORE_ALL_PWD) {
                    loginLog.setPassword("******");
                }
                if (loginLogMode == LoginLogModeEnum.IGNORE_SUCCESS_PWD && DEFAULT_LOGIN_SUCCESS_RESULT.equals(msg)) {
                    loginLog.setPassword("******");
                }
            }
            loginLog.setResult(msg);
            loginLogService.save(loginLog);
        }
    }

}
