package im.zhaojun.zfile.module.storage.controller.proxy;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.core.exception.StorageSourceNotSupportProxyUploadException;
import im.zhaojun.zfile.core.util.ProxyDownloadUrlUtils;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.beans.Beans;
import java.io.IOException;

/**
 * 服务端代理下载 Controller
 *
 * @author zhaojun
 */
@Api(tags = "服务端代理下载")
@ApiSort(6)
@Controller
public class ProxyDownloadController {

    @Resource
    private StorageSourceContext storageSourceContext;

    @Resource
    private HttpServletRequest httpServletRequest;


    @GetMapping("/pd/{storageKey}/**")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "下载本地存储源的文件", notes = "因第三方存储源都有下载地址，本接口提供本地存储的下载地址的处理, 返回文件流进行下载.")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storageKey", value = "存储源 key", dataTypeClass = String.class),
            @ApiImplicitParam(paramType = "query", name = "type",
                    value = "下载类型: download(不论什么格式的文件都进行下载操作), " +
                            "default(使用浏览器默认处理，浏览器支持预览的格式，则进行预览，不支持的则进行下载)",
                    example = "download", dataTypeClass = String.class)
    })
    @ResponseBody
    public ResponseEntity<org.springframework.core.io.Resource> downAttachment(@PathVariable("storageKey") String storageKey, String signature) throws IOException {
        // 获取下载文件路径
        String path = (String) httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) httpServletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);

        AbstractBaseFileService<?> storageServiceByKey = storageSourceContext.getByStorageKey(storageKey);

        // 如果不是 ProxyTransferService, 则返回错误信息.
        if (!Beans.isInstanceOf(storageServiceByKey, AbstractProxyTransferService.class)) {
            throw new StorageSourceNotSupportProxyUploadException("存储类型异常，不支持上传.");
        }

        // 进行上传.
        AbstractProxyTransferService<?> proxyDownloadService = (AbstractProxyTransferService<?>) storageServiceByKey;

        // 如果是私有空间才校验签名.
        boolean privateStorage = proxyDownloadService.getParam().isPrivate();
        if (privateStorage) {
            Integer storageId = proxyDownloadService.getStorageId();
            boolean valid = ProxyDownloadUrlUtils.validSignatureExpired(storageId, filePath, signature);
            if (!valid) {
                throw new IllegalArgumentException("签名无效或下载地址已过期.");
            }
        }

        return proxyDownloadService.downloadToStream(filePath);
    }

}