package im.zhaojun.zfile.core.filter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import im.zhaojun.zfile.core.constant.MdcConstant;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * MDC 过滤器, 用于写入 TraceId, 请求 IP, 用户名等信息到日志中.
 *
 * @author zhaojun
 */
@WebFilter(urlPatterns = "/*")
public class MDCFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		
		MDC.put(MdcConstant.TRACE_ID, IdUtil.fastUUID());
		MDC.put(MdcConstant.IP, JakartaServletUtil.getClientIP(httpServletRequest));
		MDC.put(MdcConstant.USER, ZFileAuthUtil.getCurrentUserId().toString());
		
		try {
			filterChain.doFilter(httpServletRequest, httpServletResponse);
		} finally {
			MDC.clear();
		}
	}
	
}