package im.zhaojun.zfile.module.link.aspect;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.extra.servlet.ServletUtil;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.annotation.LinkRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

	private TimedCache<String, AtomicInteger> timedCache;

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
		if (timedCache == null) {
			timedCache = new TimedCache<>(linkLimitSecond * 1000);
		}

		String clientIp = ServletUtil.getClientIP(httpServletRequest);
		AtomicInteger ipDownloadCount = timedCache.get(clientIp, false, () -> new AtomicInteger(1));
		if (ipDownloadCount.get() > linkDownloadLimit) {
			throw new RuntimeException("当前系统限制每 " + systemConfig.getLinkLimitSecond() + " 秒内只能访问 " + linkDownloadLimit + " 次直链, 已超出请稍后访问.");
		}
		ipDownloadCount.incrementAndGet();
		log.info("ip {}, download count: {}", clientIp, ipDownloadCount.get());
		return point.proceed();
	}

}