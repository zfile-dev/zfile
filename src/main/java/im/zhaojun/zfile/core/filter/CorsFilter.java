package im.zhaojun.zfile.core.filter;

import cn.hutool.core.util.ObjectUtil;
import im.zhaojun.zfile.core.constant.ZFileHttpHeaderConstant;
import im.zhaojun.zfile.core.util.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 开启跨域支持. 一般用于开发环境, 或前后端分离部署时开启.
 *
 * @author zhaojun
 */
@WebFilter(urlPatterns = "/*")
@Order(Integer.MIN_VALUE)
@Component
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (httpServletRequest.getRequestURI().equals("/favicon.ico")) {
            return;
        }
    
        String header = httpServletRequest.getHeader(HttpHeaders.ORIGIN);

        List<String> allowHeaders = Arrays.asList("Origin", "X-Requested-With", "Content-Type", "Accept", ZFileHttpHeaderConstant.ZFILE_TOKEN, ZFileHttpHeaderConstant.AXIOS_REQUEST, ZFileHttpHeaderConstant.AXIOS_FROM);
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ObjectUtil.defaultIfNull(header, "*"));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.join(",", allowHeaders));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "600");

        if (!CorsUtils.isPreFlightRequest(httpServletRequest)) {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

}