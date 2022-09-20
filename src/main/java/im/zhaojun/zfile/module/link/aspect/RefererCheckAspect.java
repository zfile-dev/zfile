package im.zhaojun.zfile.module.link.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.module.storage.annotation.RefererCheck;
import im.zhaojun.zfile.module.link.model.enums.RefererTypeEnum;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

/**
 * 校验 referer 防盗链.
 * <p>
 * 校验所有标注了 {@link RefererCheck} 的注解
 *
 * @author zhaojun
 */
@Aspect
@Component
@Slf4j
public class RefererCheckAspect {

	@Resource
	private HttpServletRequest httpServletRequest;

	@Resource
	private HttpServletResponse httpServletResponse;

	@Resource
	private SystemConfigService systemConfigService;

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * 校验 referer 防盗链.
	 *
	 * @param   point
	 *          连接点
	 *
	 * @return  方法运行结果
	 */
	@Around(value = "@annotation(im.zhaojun.zfile.module.storage.annotation.RefererCheck)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		// 获取配置的 referer 类型
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
		RefererTypeEnum refererType = systemConfig.getRefererType();

		// 如果未开启 referer 防盗链则跳过.
		if (refererType == RefererTypeEnum.OFF) {
			return point.proceed();
		}

		// 获取当前请求 referer
		String referer = httpServletRequest.getHeader(HttpHeaders.REFERER);
		String requestUrl = httpServletRequest.getRequestURI();

		// 获取 Forbidden 页面地址
		String forbiddenUrl = systemConfigService.getForbiddenUrl();

		// 如果 referer 不允许为空，且当前 referer 为空，则校验
		Boolean refererAllowEmpty = systemConfig.getRefererAllowEmpty();
		if (!refererAllowEmpty && StrUtil.isEmpty(referer)) {
			log.warn("请求路径 {}, referer 不允许为空，当前请求 referer 为空，禁止访问.", requestUrl);
			httpServletResponse.sendRedirect(forbiddenUrl);
			return null;
		}

		// 获取允许的 referer 地址
		String refererValue = systemConfig.getRefererValue();
		List<String> refererValueList = StrUtil.split(refererValue, '\n');

		// 如果是白名单模式，则校验当前 referer, 如果未在允许的列表中，则禁止访问.
		if (refererType == RefererTypeEnum.WHITE_LIST && !containsPathMatcher(refererValueList, referer)) {
			log.warn("请求路径 {}, referer 为白名单模式，当前请求 referer {} 未在白名单中，禁止访问.", requestUrl, referer);
			httpServletResponse.sendRedirect(forbiddenUrl);
			return null;
		}

		// 如果是黑名单模式，则校验当前 referer 是否在列表中，则禁止访问.
		if (refererType == RefererTypeEnum.BLACK_LIST && containsPathMatcher(refererValueList, referer)) {
			log.warn("请求路径 {}, referer 为黑名单模式，当前请求 referer {} 在黑名单中，禁止访问.", requestUrl, referer);

			httpServletResponse.sendRedirect(forbiddenUrl);
			return null;
		}

		return point.proceed();
	}

	/**
	 * 校验 value 是否在 Ant 表达式列表中.
	 *
	 * @param   patternList
	 *          Ant 表达式列表
	 *
	 * @param   value
	 *          要校验的值
	 *
	 * @return  如果集合为空 (null 或者空), 返回 false, 否则在表达式列表中找到匹配的返回 true, 找不到返回 false.
	 */
	public boolean containsPathMatcher(Collection<String> patternList, String value) {
		if (CollUtil.isEmpty(patternList)) {
			return false;
		}

		for (String pattern : patternList) {
			if (pathMatcher.match(pattern, value)) {
				return true;
			}
		}

		return false;
	}

}