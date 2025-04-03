package im.zhaojun.zfile.core.filter;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.core.constant.RuleTypeConstant;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.matcher.IRuleMatcher;
import im.zhaojun.zfile.core.util.matcher.RuleMatcherFactory;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

/**
 * 检测访问的 IP 和 UA 是否符合系统安全设置中的规则
 *
 * @author zhaojun
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    private static volatile SystemConfigService systemConfigService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // 双重检测锁, 防止多次初始化
        if (systemConfigService == null) {
            synchronized (this) {
                if (systemConfigService == null) {
                    systemConfigService = SpringUtil.getBean(SystemConfigService.class);
                }
            }
        }

        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String accessIpBlocklist = systemConfig.getAccessIpBlocklist();
        String accessUaBlocklist = systemConfig.getAccessUaBlocklist();

        // 判断当前访问 IP 是否在黑名单中
        String currentAccessIp = JakartaServletUtil.getClientIP(httpServletRequest);
        if (StringUtils.isNotBlank(accessIpBlocklist) && checkIsDisableIP(accessIpBlocklist, currentAccessIp)) {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            httpServletResponse.getWriter().write("disable access.[" + currentAccessIp + "]");
            return;
        }

        // 判断当前访问 User-Agent 是否在黑名单中
        String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);
        if (StringUtils.isNotBlank(accessUaBlocklist) && checkIsDisableUA(accessUaBlocklist, userAgent)) {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            httpServletResponse.getWriter().write("disable access.[" + userAgent + "]");
            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private boolean checkIsDisableIP(String accessIpBlocklist, String currentAccessIp) {
        IRuleMatcher ruleMatcher = RuleMatcherFactory.getRuleMatcher(RuleTypeConstant.IP);
        List<String> ruleList = StringUtils.split(accessIpBlocklist, StringUtils.LF);
        return ruleMatcher.matchAny(ruleList, currentAccessIp);
    }

    private boolean checkIsDisableUA(String accessUaBlocklist, String currentAccessUA) {
        IRuleMatcher ruleMatcher = RuleMatcherFactory.getRuleMatcher(RuleTypeConstant.SPRING_SIMPLE);
        List<String> ruleList = StringUtils.split(accessUaBlocklist, StringUtils.LF);
        return ruleMatcher.matchAny(ruleList, currentAccessUA);
    }

}