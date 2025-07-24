package im.zhaojun.zfile.core.controller;

import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;

/**
 * 处理前端首页 Controller
 *
 * @author zhaojun
 */
@Slf4j
@Controller
public class FrontIndexController {

	@Resource
	private SystemConfigService systemConfigService;

	@Resource
	private WebProperties webProperties;

	/**
	 * 所有未找到的页面都跳转到首页, 用户解决 vue history 直接访问 404 的问题
	 * 同时, 读取 index.html 文件, 修改 title 和 favicon 后返回.
	 *
	 * @return  转发到 /index.html
	 */
	@RequestMapping(value = { "/"})
	@ResponseBody
	public ResponseEntity<String> redirect() {
		// 读取 resources/static/index.html 文件修改 title 和 favicon 后返回
		ResourceLoader resourceLoader = new FileSystemResourceLoader();
		String[] staticLocations = webProperties.getResources().getStaticLocations();

		// 如果 staticLocations 里没有包含 file:static/, 则手动添加
		boolean fileStaticExist = false;
		for (String staticLocation : staticLocations) {
			if (staticLocation.startsWith("file:")) {
				fileStaticExist = true;
				break;
			}
		}
		if (!fileStaticExist) {
			staticLocations = org.apache.commons.lang3.ArrayUtils.add(staticLocations, "file:static/");
		}

		for (String staticLocation : staticLocations) {
			org.springframework.core.io.Resource resource = resourceLoader.getResource(staticLocation + "/index.html");
			boolean exists = resource.exists();
			if (exists) {
				String content;
				try {
					content = resource.getContentAsString(StandardCharsets.UTF_8);
					if (log.isTraceEnabled()) {
						log.trace("读取 index.html 文件成功, 文件路径: {}", staticLocation);
					}
				} catch (Exception e) {
					log.error("{} 资源存在但读取 index.html 文件失败.", staticLocation);
					return ResponseEntity.status(500).body("static index.html read error");
				}

				SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();

				// 替换为系统设置中的站点名称
				String siteName = systemConfig.getSiteName();
				if (StringUtils.isNotBlank(siteName)) {
					content = content.replace("<title>ZFile</title>", "<title>" + siteName + "</title>");
				}

				// 替换为系统设置中的 favicon 地址
				String faviconUrl = systemConfig.getFaviconUrl();
				if (StringUtils.isNotBlank(faviconUrl)) {
					content = content.replace("/favicon.svg", faviconUrl);
				}

				// 添加缓存控制头
				return ResponseEntity.ok()
						.header("Cache-Control", "max-age=600, must-revalidate, proxy-revalidate")						.header("Pragma", "no-cache")
						.body(content);
			}
		}

		return ResponseEntity.status(404).body("static index.html not found");
	}

	@RequestMapping(value = { "/guest"})
	@ResponseBody
	public String guest() {
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
		return systemConfig.getGuestIndexHtml();
	}

}