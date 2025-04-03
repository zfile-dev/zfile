package im.zhaojun.zfile.module.link.aspect;

import cn.hutool.extra.servlet.JakartaServletUtil;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.cache.LinkRateLimiterCache;
import im.zhaojun.zfile.module.storage.annotation.LinkRateLimiter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 校验直链访问频率.
 * <p>
 * 校验所有标注了 {@link LinkRateLimiter} 的注解
 *
 * @author zhaojun
 */
@Aspect
@Component
@Slf4j
public class LinkRateLimiterAspect {

	@Resource
	private HttpServletRequest httpServletRequest;

	@Resource
	private SystemConfigService systemConfigService;

	@Resource
	private LinkRateLimiterCache linkRateLimiterCache;

	/**
	 * 校验直链访问频率.
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around(value = "@annotation(im.zhaojun.zfile.module.storage.annotation.LinkRateLimiter)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
		Integer linkLimitSecond = systemConfig.getLinkLimitSecond();
		Integer linkDownloadLimit = systemConfig.getLinkDownloadLimit();
		// 如果未设置直链限制, 则不进行校验
		if (linkLimitSecond == null || linkDownloadLimit == null || linkLimitSecond == 0 || linkDownloadLimit == 0) {
			return point.proceed();
		}

		String clientIP = JakartaServletUtil.getClientIP(httpServletRequest);
		if (linkRateLimiterCache.containsKey(clientIP)) {
			AtomicInteger atomicInteger = linkRateLimiterCache.get(clientIP, false);
			if (atomicInteger.incrementAndGet() > linkDownloadLimit) {
				throw new BizException(ErrorCode.BIZ_ACCESS_TOO_FREQUENT);
			}
		} else {
			linkRateLimiterCache.put(clientIP, new AtomicInteger(1), linkLimitSecond * 1000);
		}

		return point.proceed();
	}

}