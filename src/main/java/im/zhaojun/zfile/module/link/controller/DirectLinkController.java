package im.zhaojun.zfile.module.link.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.SpringMvcUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.model.request.BatchGenerateLinkRequest;
import im.zhaojun.zfile.module.link.model.result.BatchGenerateLinkResponse;
import im.zhaojun.zfile.module.link.service.DynamicDirectLinkPrefixService;
import im.zhaojun.zfile.module.link.service.LinkDownloadService;
import im.zhaojun.zfile.module.storage.annotation.StoragePermissionCheck;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 短链接口
 *
 * @author zhaojun
 */
@Tag(name = "短链")
@ApiSort(5)
@Controller
@Slf4j
public class DirectLinkController {

    @Resource
    private LinkDownloadService linkDownloadService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DynamicDirectLinkPrefixService dynamicDirectLinkPrefixService;

    public static final String DIRECT_LINK_SUFFIX_PATH = "/{storageKey}/**";

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws NoSuchMethodException {
        String directLinkPrefix = systemConfigService.getSystemConfig().getDirectLinkPrefix();
        Method directLinkMethod = DirectLinkController.class.getMethod("directLink", String.class);
        RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(directLinkPrefix + DIRECT_LINK_SUFFIX_PATH).build();
        dynamicDirectLinkPrefixService.registerMappingHandlerMapping(SystemConfig.DIRECT_LINK_PREFIX_NAME, requestMappingInfo, this, directLinkMethod);
    }

    /**
     * 路径直链处理方法，会根据 URL 中的存储源 key 和文件路径, 获取到文件，判断文件是否有短链，没有则生成，然后跳转到短链.
     *
     * @param   storageKey
     *          存储源 key
     */
    public ResponseEntity<?> directLink(@PathVariable("storageKey") String storageKey) throws IOException {
        // 获取直链全路径
        String filePath = SpringMvcUtils.getExtractPathWithinPattern();

        // 如果路径不是以 / 开头, 则补充上
        if (StringUtils.isNotEmpty(filePath) && filePath.charAt(0) != StringUtils.SLASH_CHAR) {
            filePath = StringUtils.SLASH + filePath;
        }

        return linkDownloadService.handlerDirectLink(storageKey, filePath);
    }

    @PostMapping("/api/path-link/batch/generate")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @Operation(summary = "生成路径直链", description ="对指定存储源的某文件路径生成路径直链")
    @StoragePermissionCheck(action = FileOperatorTypeEnum.LINK)
    public AjaxJson<List<BatchGenerateLinkResponse>> generatorShortLink(@RequestBody @Valid BatchGenerateLinkRequest batchGenerateLinkRequest) {
        List<BatchGenerateLinkResponse> result = new ArrayList<>();

        // 获取站点域名
        String serverAddress = systemConfigService.getAxiosFromDomainOrSetting();
        String directLinkPrefix = systemConfigService.getSystemConfig().getDirectLinkPrefix();

        String storageKey = batchGenerateLinkRequest.getStorageKey();

        AbstractBaseFileService<?> baseFileService = StorageSourceContext.getByStorageKey(storageKey);
        if (baseFileService == null) {
            throw new BizException(ErrorCode.BIZ_STORAGE_NOT_FOUND);
        }
        String currentUserBasePath = baseFileService.getCurrentUserBasePath();

        for (String path : batchGenerateLinkRequest.getPaths()) {
            // 拼接全路径地址.
            String fullPath = StringUtils.concat(serverAddress, directLinkPrefix, storageKey, currentUserBasePath, path);
            result.add(new BatchGenerateLinkResponse(fullPath));
        }
        return AjaxJson.getSuccessData(result);
    }
}