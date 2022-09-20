package im.zhaojun.zfile.module.storage.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.log.service.DownloadLogService;
import im.zhaojun.zfile.module.filter.service.FilterConfigService;
import im.zhaojun.zfile.module.link.service.ShortLinkService;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.core.constant.ZFileConstant;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * 下载链接过滤器，如前缀是设置的直链，则进行直链处理
 *
 * @author zhaojun
 */
@WebFilter(urlPatterns = "/*")
@Slf4j
public class DownloadLinkFilter implements Filter {

	private SystemConfigService systemConfigService;

	private StorageSourceService storageSourceService;

	private StorageSourceContext storageSourceContext;

	private DownloadLogService downloadLogService;

	private ShortLinkService shortLinkService;

	private FilterConfigService filterConfigService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (systemConfigService == null) {
			systemConfigService = SpringUtil.getBean(SystemConfigService.class);
		}

		if (storageSourceService == null) {
			storageSourceService = SpringUtil.getBean(StorageSourceService.class);
		}

		if (storageSourceContext == null) {
			storageSourceContext = SpringUtil.getBean(StorageSourceContext.class);
		}

		if (downloadLogService == null) {
			downloadLogService = SpringUtil.getBean(DownloadLogService.class);
		}

		if (shortLinkService == null) {
			shortLinkService = SpringUtil.getBean(ShortLinkService.class);
		}

		if (filterConfigService == null) {
			filterConfigService = SpringUtil.getBean(FilterConfigService.class);
		}

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		String requestUrl = httpServletRequest.getRequestURI();

		// 获取路径层级，少于 2 层的不可能是直链
		List<String> list = StrUtil.split(requestUrl, '/', true, true);
		if (CollUtil.size(list) > 2) {
			// 获取当前请求 URL 的前缀
			String currentRequestPrefix = list.get(0);
			// 获取存储源 key
			String currentStorageKey = list.get(1);

			// 获取下载文件路径
			List<String> pathList = CollUtil.sub(list, 2, list.size());
			String filePath = CollUtil.join(pathList, StringUtils.DELIMITER_STR);

			// 获取系统配置的直链前缀
			SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
			String directLinkPrefix = systemConfig.getDirectLinkPrefix();
			if (StrUtil.equalsIgnoreCase(currentRequestPrefix, directLinkPrefix)) {
				
				if (BooleanUtil.isFalse(systemConfig.getShowPathLink())) {
					httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
					httpServletResponse.getWriter().write("当前系统不允许使用直链.");
					return;
				}

				// 获取下载地址全路径, 不以 / 开头的要补 /, 进行了 url encode 编码的要进行解码
				String decodeFilePath = StringUtils.concat(ZFileConstant.PATH_SEPARATOR, StringUtils.decode(filePath));

				// 根据存储源 key 获取存储源 id.
				Integer storageId = storageSourceService.findIdByKey(currentStorageKey);
				if (filterConfigService.checkFileIsDisableDownload(storageId, decodeFilePath)) {
					// 获取 Forbidden 页面地址
					String forbiddenUrl = systemConfigService.getForbiddenUrl();
					httpServletResponse.sendRedirect(forbiddenUrl);
					return;
				}
				handleDownloadLink(httpServletResponse, storageId, currentStorageKey, decodeFilePath);
				return;
			}
		}

		chain.doFilter(httpServletRequest, httpServletResponse);
	}


	/**
	 * 进行文件下载, 跳转到指定下载地址
	 *
	 * @param   response
	 *          HttpServletResponse
	 *
	 * @param   storageKey
	 *          存储源 key
	 *
	 * @param   filePath
	 *          文件路径
	 */
	private void handleDownloadLink(HttpServletResponse response, Integer storageId, String storageKey, String filePath) throws IOException {
		StorageSource storageSource = storageSourceService.findByStorageKey(storageKey);
		Boolean enable = storageSource.getEnable();
		if (!enable) {
			log.warn("存储源当前状态为未启用，仍然请求下载，存储源 key {}, 文件路径 {}", storageKey, filePath);
			response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
			response.getWriter().write("当前存储源未启用, 无法下载，请联系管理员!");
			return;
		}

		if (filePath.length() > 0 && filePath.charAt(0) != ZFileConstant.PATH_SEPARATOR_CHAR) {
			filePath = "/" + filePath;
		}

		ShortLink shortLink = shortLinkService.findByStorageIdAndUrl(storageId, filePath);
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
		
		// 如果没有短链，则生成短链
		if (shortLink == null) {
			if (BooleanUtil.isFalse(systemConfig.getAllowPathLinkAnonAccess())) {
				log.warn("存储源「{}」未启用 \"允许路径直链可直接访问\" 功能，仍然进行了访问路径直链: {}", storageKey, filePath);
				response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
				response.getWriter().write("该文件未生成直链, 无法下载，请联系管理员!");
				return;
			}
			
			shortLink = shortLinkService.generatorShortLink(storageId, filePath);
		}

		shortLinkService.handlerDownload(storageKey, filePath, shortLink.getShortKey());
	}

}