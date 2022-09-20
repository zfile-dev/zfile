package im.zhaojun.zfile.core.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.ServletUtil;
import im.zhaojun.zfile.core.constant.MdcConstant;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhaojun
 */
@WebFilter(urlPatterns = "/*")
public class MDCFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		
		MDC.put(MdcConstant.TRACE_ID, IdUtil.fastUUID());
		MDC.put(MdcConstant.IP, ServletUtil.getClientIP(httpServletRequest));
		MDC.put(MdcConstant.USER, StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "anonymous");
		
		try {
			filterChain.doFilter(httpServletRequest, httpServletResponse);
		} finally {
			MDC.clear();
		}
	}
	
}