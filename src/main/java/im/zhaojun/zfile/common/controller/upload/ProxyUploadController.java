package im.zhaojun.zfile.common.controller.upload;

import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.common.util.AjaxJson;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.home.service.base.ProxyTransferService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.beans.Beans;
import java.io.IOException;

/**
 * 服务端代理上传 Controller
 *
 * @author zhaojun
 */
@RestController
public class ProxyUploadController {

	@Resource
	private StorageSourceContext storageSourceContext;

	@Resource
	private HttpServletRequest httpServletRequest;


	@PostMapping("/file/upload/{storageKey}/**")
	@ResponseBody
	public AjaxJson<?> upload(@RequestParam MultipartFile file, @PathVariable("storageKey") String storageKey) throws IOException {
		if (file == null || file.isEmpty()) {
			return AjaxJson.getError("文件为空，无法上传.");
		}

		// 获取上传路径
		String path = (String) httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String) httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		AntPathMatcher apm = new AntPathMatcher();
		String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);

		AbstractBaseFileService<?> storageServiceByKey = storageSourceContext.getByKey(storageKey);

		// 如果不是 ProxyTransferService, 则返回错误信息.
		if (!Beans.isInstanceOf(storageServiceByKey, ProxyTransferService.class)) {
			return AjaxJson.getError("存储类型异常，不支持上传.");
		}


		// 进行上传.
		ProxyTransferService<?> proxyUploadService = (ProxyTransferService<?>) storageServiceByKey;
		proxyUploadService.uploadFile(filePath, file.getInputStream());
		return AjaxJson.getSuccess();
	}

}