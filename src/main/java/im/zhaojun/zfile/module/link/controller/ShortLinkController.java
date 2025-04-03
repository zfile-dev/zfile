package im.zhaojun.zfile.module.link.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.link.model.entity.ShortLink;
import im.zhaojun.zfile.module.link.model.request.BatchGenerateLinkRequest;
import im.zhaojun.zfile.module.link.model.result.BatchGenerateLinkResponse;
import im.zhaojun.zfile.module.link.service.LinkDownloadService;
import im.zhaojun.zfile.module.link.service.ShortLinkService;
import im.zhaojun.zfile.module.storage.annotation.StoragePermissionCheck;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 短链接口
 *
 * @author zhaojun
 */
@Tag(name = "直短链模块")
@ApiSort(5)
@Controller
@Slf4j
public class ShortLinkController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ShortLinkService shortLinkService;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private LinkDownloadService linkDownloadService;

    @PostMapping("/api/short-link/batch/generate")
    @ResponseBody
    @ApiOperationSupport(order = 1)
    @Operation(summary = "生成短链", description ="对指定存储源的某文件路径生成短链")
    @StoragePermissionCheck(action = FileOperatorTypeEnum.SHORT_LINK)
    public AjaxJson<List<BatchGenerateLinkResponse>> generatorShortLink(@RequestBody @Valid BatchGenerateLinkRequest batchGenerateLinkRequest) {
        List<BatchGenerateLinkResponse> result = new ArrayList<>();

        // 获取站点域名
        String domain = systemConfigService.getAxiosFromDomainOrSetting();
        Long expireTime = batchGenerateLinkRequest.getExpireTime();
        String storageKey = batchGenerateLinkRequest.getStorageKey();
        Integer storageId = storageSourceService.findIdByKey(storageKey);

        for (String path : batchGenerateLinkRequest.getPaths()) {
            // 拼接全路径地址.
            String currentUserBasePath = StorageSourceContext.getByStorageId(storageId).getCurrentUserBasePath();
            String fullPath = StringUtils.concat(currentUserBasePath, path);
            ShortLink shortLink = shortLinkService.generatorShortLink(storageId, fullPath, expireTime);
            String shortUrl = StringUtils.removeDuplicateSlashes(domain + "/s/" + shortLink.getShortKey());
            result.add(new BatchGenerateLinkResponse(shortUrl));
        }
        return AjaxJson.getSuccessData(result);
    }


    @GetMapping("/s/{key}")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "跳转短链", description ="根据短链 key 跳转（302 重定向）到对应的直链.")
    @Parameter(in = ParameterIn.PATH, name = "key", description = "短链 key", required = true, schema = @Schema(type = "string"))
    public ResponseEntity<?> parseShortKey(@PathVariable String key) throws IOException {
        return linkDownloadService.handlerShortLink(key);
    }

}