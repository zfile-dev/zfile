package im.zhaojun.zfile.home.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.admin.model.entity.DownloadLog;
import im.zhaojun.zfile.admin.model.entity.ShortLink;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.service.DownloadLogService;
import im.zhaojun.zfile.admin.service.FilterConfigService;
import im.zhaojun.zfile.admin.service.ShortLinkService;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.admin.service.SystemConfigService;
import im.zhaojun.zfile.common.constant.ZFileConstant;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.common.exception.InvalidStorageSourceException;
import im.zhaojun.zfile.common.exception.file.operator.DownloadFileException;
import im.zhaojun.zfile.common.util.HttpUtil;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EncodingUtils;
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
import java.io.OutputStream;
import java.util.Date;
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
				if (filterConfigService.filterResultIsDisableDownload(storageId, decodeFilePath)) {
					// 获取 Forbidden 页面地址
					String forbiddenUrl = systemConfigService.getForbiddenUrl();
					httpServletResponse.sendRedirect(forbiddenUrl);
					return;
				}

				Boolean recordDownloadLog = systemConfig.getRecordDownloadLog();
				if (BooleanUtil.isTrue(recordDownloadLog)) {
					DownloadLog downloadLog = new DownloadLog();
					downloadLog.setPath(decodeFilePath);
					downloadLog.setStorageKey(currentStorageKey);
					downloadLog.setCreateTime(new Date());
					downloadLog.setIp(ServletUtil.getClientIP(httpServletRequest));
					downloadLog.setReferer(httpServletRequest.getHeader(HttpHeaders.REFERER));
					downloadLog.setUserAgent(httpServletRequest.getHeader(HttpHeaders.USER_AGENT));

					ShortLink shortLink = shortLinkService.findByStorageIdAndUrl(storageId, decodeFilePath);
					// 如果没有短链，则生成短链
					if (shortLink == null) {
						shortLink = shortLinkService.generatorShortLink(storageId, decodeFilePath);
					}
					downloadLog.setShortKey(shortLink.getShortKey());

					downloadLogService.save(downloadLog);
				}
				handleDownloadLink(httpServletResponse, currentStorageKey, decodeFilePath);
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
	private void handleDownloadLink(HttpServletResponse response, String storageKey, String filePath) throws IOException {
		StorageSource storageSource = storageSourceService.findByStorageKey(storageKey);
		Boolean enable = storageSource.getEnable();
		if (!enable) {
			log.error("存储源当前状态为未启用，仍然请求下载，存储源 key {}, 文件路径 {}", storageKey, filePath);
			response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
			response.getWriter().write("当前存储源未启用, 无法下载，请联系管理员!");
			return;
		}

		if (filePath.length() > 0 && filePath.charAt(0) != ZFileConstant.PATH_SEPARATOR_CHAR) {
			filePath = "/" + filePath;
		}

		AbstractBaseFileService<?> fileService;
		try {
			fileService = storageSourceContext.getByKey(storageKey);
		} catch (InvalidStorageSourceException e) {
			log.error("无效的存储源，存储源 key {}, 文件路径 {}", storageKey, filePath);
			response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
			response.getWriter().write("无效的或初始化失败的存储源, 请联系管理员!");
			return;
		}

		String downloadUrl;
		try {
			downloadUrl = fileService.getDownloadUrl(filePath);
		} catch (DownloadFileException e) {
			log.error("获取文件下载链接异常 {}. 存储源 ID: {}, 文件路径: {}", e.getMessage(), e.getStorageId(), e.getPathAndName());
			response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
			response.getWriter().write("获取下载链接异常，请联系管理员!");
			return;
		}

		if (StrUtil.isEmpty(downloadUrl)) {
			log.error("获取到文件下载链接为空，存储源 key {}, 文件路径 {}", storageKey, filePath);
			response.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8");
			response.getWriter().write("获取下载链接异常，请联系管理员![2]");
			return;
		}

		if (StrUtil.equalsIgnoreCase(FileUtil.extName(filePath), "m3u8")) {
			String textContent = HttpUtil.getTextContent(downloadUrl);
			response.setContentType("application/vnd.apple.mpegurl;charset=utf-8");
			OutputStream outputStream = response.getOutputStream();
			byte[] textContentBytes = EncodingUtils.getBytes(textContent, CharsetUtil.CHARSET_UTF_8.displayName());
			IoUtil.write(outputStream, true, textContentBytes);
			return;
		}

		// 禁止直链被浏览器 302 缓存.
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private");
		response.setHeader(HttpHeaders.PRAGMA, "no-cache");
		response.setHeader(HttpHeaders.EXPIRES, "0");

		response.sendRedirect(downloadUrl);
	}

}