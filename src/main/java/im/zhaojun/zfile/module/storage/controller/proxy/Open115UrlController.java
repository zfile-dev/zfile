package im.zhaojun.zfile.module.storage.controller.proxy;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.storage.service.impl.Open115ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Open115UrlController {

    public static final String PROXY_DOWNLOAD_LINK_PREFIX = "/open115/url/";

    @GetMapping(PROXY_DOWNLOAD_LINK_PREFIX + "{storageId}/{pickCode}")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @Operation(summary = "跳转 115 网盘实际下载地址", description ="根据 115 文件提取码跳转（302 重定向）到实际下载地址.")
    @Parameter(in = ParameterIn.PATH, name = "pickCode", description = "文件提取码", required = true, schema = @Schema(type = "string"))
    public ResponseEntity<?> redirectTo115DownloadUrl(@PathVariable Integer storageId, @PathVariable String pickCode) {
        AbstractBaseFileService<?> fileService = StorageSourceContext.getByStorageId(storageId);
        if (fileService instanceof Open115ServiceImpl open115Service) {
            String downloadUrlByPickCode = open115Service.getOpen115DownloadUrlByPickCode(pickCode);
            return ResponseEntity.status(302)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header(HttpHeaders.LOCATION, downloadUrlByPickCode)
                    .build();
        } else {
            throw new BizException(ErrorCode.BIZ_UNSUPPORTED_OPERATION_TYPE);
        }
    }
}
