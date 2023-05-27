package im.zhaojun.zfile.module.storage.controller.proxy;

import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.config.utils.SpringMvcUtils;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.beans.Beans;
import java.io.IOException;

/**
 * 服务端代理上传 Controller
 *
 * @author zhaojun
 */
@Api(tags = "服务端代理上传")
@RestController
public class ProxyUploadController {

	@Resource
	private StorageSourceContext storageSourceContext;

	@Resource
	private HttpServletRequest httpServletRequest;


	@PostMapping("/file/upload/{storageKey}/**")
	@ResponseBody
	public AjaxJson<?> upload(@RequestParam MultipartFile file, @PathVariable("storageKey") String storageKey) throws IOException {
		if (file == null) {
			throw new RuntimeException("空文件不能为空");
		}

		// 获取上传路径
		String filePath = SpringMvcUtils.getExtractPathWithinPattern();

		AbstractBaseFileService<?> storageServiceByKey = storageSourceContext.getByStorageKey(storageKey);

		// 如果不是 ProxyTransferService, 则返回错误信息.
		if (!Beans.isInstanceOf(storageServiceByKey, AbstractProxyTransferService.class)) {
			return AjaxJson.getError("存储类型异常，不支持上传.");
		}


		// 进行上传.
		AbstractProxyTransferService<?> proxyUploadService = (AbstractProxyTransferService<?>) storageServiceByKey;
		proxyUploadService.uploadFile(filePath, file.getInputStream());
		return AjaxJson.getSuccess();
	}

}