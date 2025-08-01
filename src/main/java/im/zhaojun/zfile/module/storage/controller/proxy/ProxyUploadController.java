package im.zhaojun.zfile.module.storage.controller.proxy;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.SpringMvcUtils;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Beans;

/**
 * 服务端代理上传 Controller
 *
 * @author zhaojun
 */
@Tag(name = "服务端代理上传")
@RestController
public class ProxyUploadController {

	@PutMapping("/file/upload/{storageKey}/**")
	@ResponseBody
	public AjaxJson<?> upload(@RequestParam MultipartFile file, @PathVariable("storageKey") String storageKey, @RequestParam(value = "filename", required = false) String filename) throws Exception {
		if (file == null) {
			throw new BizException(ErrorCode.BIZ_UPLOAD_FILE_NOT_EMPTY);
		}

		// 获取上传路径
		String filePath = SpringMvcUtils.getExtractPathWithinPattern();
		filePath = filename != null ? filePath + StringUtils.SLASH + filename : filePath;

		AbstractBaseFileService<?> storageServiceByKey = StorageSourceContext.getByStorageKey(storageKey);

		// 如果不是 ProxyTransferService, 则返回错误信息.
		if (storageServiceByKey == null || !Beans.isInstanceOf(storageServiceByKey, AbstractProxyTransferService.class)) {
			return AjaxJson.getError("存储类型异常，不支持上传.");
		}

		// 进行上传.
		AbstractProxyTransferService<?> proxyUploadService = (AbstractProxyTransferService<?>) storageServiceByKey;
		proxyUploadService.uploadFile(filePath, file.getInputStream(), file.getSize());
		return AjaxJson.getSuccess();
	}

}