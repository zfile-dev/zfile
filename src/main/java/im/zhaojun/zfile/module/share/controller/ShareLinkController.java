package im.zhaojun.zfile.module.share.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.share.model.request.CreateShareLinkRequest;
import im.zhaojun.zfile.module.share.model.request.ShareFileListRequest;
import im.zhaojun.zfile.module.share.model.request.ShareLinkListRequest;
import im.zhaojun.zfile.module.share.model.request.VerifySharePasswordRequest;
import im.zhaojun.zfile.module.share.model.result.CreateShareLinkResult;
import im.zhaojun.zfile.module.share.model.result.ShareFileInfoResult;
import im.zhaojun.zfile.module.share.model.result.ShareLinkResult;
import im.zhaojun.zfile.module.share.service.ShareLinkFileService;
import im.zhaojun.zfile.module.share.service.ShareLinkService;
import im.zhaojun.zfile.module.storage.annotation.StoragePermissionCheck;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分享文件相关接口
 *
 * @author zhaojun
 */
@Tag(name = "分享文件模块")
@ApiSort(3)
@Slf4j
@RequestMapping("/api/share")
@RestController
public class ShareLinkController {

    @Resource
    private ShareLinkService shareLinkService;
    
    @Resource
    private ShareLinkFileService shareLinkFileService;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "创建分享链接", description = "根据指定的文件或文件夹创建分享链接")
    @PostMapping("/create")
    @StoragePermissionCheck(action = FileOperatorTypeEnum.SHARE_LINK)
    public AjaxJson<CreateShareLinkResult> createShareLink(@Valid @RequestBody CreateShareLinkRequest request) {
        CreateShareLinkResult result = shareLinkService.createShareLink(request);
        return AjaxJson.getSuccessData(result);
    }

    @ApiOperationSupport(order = 2)
    @Operation(summary = "获取分享信息", description = "根据分享链接key获取分享的基本信息，无需密码")
    @GetMapping("/info/{shareKey}")
    @Parameter(in = ParameterIn.PATH, name = "shareKey", description = "分享链接key", required = true, schema = @Schema(type = "string"))
    public AjaxJson<ShareLinkResult> getShareInfo(@PathVariable String shareKey) {
        ShareLinkResult result = shareLinkService.getShareLinkInfo(shareKey);
        return AjaxJson.getSuccessData(result);
    }

    @ApiOperationSupport(order = 3)
    @Operation(summary = "验证分享密码", description = "验证分享链接的访问密码")
    @PostMapping("/verify")
    public AjaxJson<Boolean> verifyPassword(@Valid @RequestBody VerifySharePasswordRequest request) {
        boolean isValid = shareLinkService.verifyPassword(request.getShareKey(), request.getPassword());
        return AjaxJson.getSuccessData(isValid);
    }

    @ApiOperationSupport(order = 4)
    @Operation(summary = "获取分享文件列表", description = "获取分享链接中的文件和文件夹列表")
    @PostMapping("/files")
    public AjaxJson<ShareFileInfoResult> getShareFileList(@Valid @RequestBody ShareFileListRequest request) {
        // 处理请求参数默认值
        request.handleDefaultValue();

        ShareFileInfoResult result = shareLinkFileService.getShareFileList(
                request.getShareKey(),
                request.getPath(),
                request.getPassword(),
                request.getFolderPassword(),
                request.getOrderBy(),
                request.getOrderDirection()
        );
        
        return AjaxJson.getSuccessData(result);
    }

    @ApiOperationSupport(order = 5)
    @Operation(summary = "下载分享文件", description = "通过分享链接下载文件，返回302重定向到实际下载地址")
    @GetMapping("/download/{shareKey}")
    @Parameter(in = ParameterIn.PATH, name = "shareKey", description = "分享链接key", required = true, schema = @Schema(type = "string"))
    @Parameter(in = ParameterIn.QUERY, name = "path", description = "文件路径", required = true, schema = @Schema(type = "string"))
    @Parameter(in = ParameterIn.QUERY, name = "password", description = "分享密码", schema = @Schema(type = "string"))
    public ResponseEntity<?> downloadShareFile(
            @PathVariable String shareKey,
            @RequestParam String path,
            @RequestParam(required = false) String password) {
        try {
            // 获取实际下载地址
            String actualDownloadUrl = shareLinkFileService.getShareFileDownloadUrl(shareKey, path, password);
            
            // 302重定向到实际地址
            return ResponseEntity.status(302)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate, private")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header(HttpHeaders.LOCATION, actualDownloadUrl)
                    .build();
                    
        } catch (Exception e) {
            log.error("分享文件下载失败, shareKey: {}, path: {}", shareKey, path, e);
            return ResponseEntity.status(400).body(AjaxJson.getError(e.getMessage()));
        }
    }

    @ApiOperationSupport(order = 6)
    @Operation(summary = "取消分享", description = "删除指定的分享链接")
    @DeleteMapping("/{shareKey}")
    @Parameter(in = ParameterIn.PATH, name = "shareKey", description = "分享链接key", required = true, schema = @Schema(type = "string"))
    @SaCheckLogin
    public AjaxJson<Boolean> deleteShare(@PathVariable String shareKey) {
        shareLinkService.deleteShareLink(shareKey);
        return AjaxJson.getSuccessData(true);
    }

    @ApiOperationSupport(order = 7)
    @Operation(summary = "获取用户分享列表", description = "获取当前用户创建的分享链接（支持分页与筛选）")
    @GetMapping("/list")
    public AjaxJson<List<ShareLinkResult>> getUserShareList(@Valid ShareLinkListRequest request) {
        Page<ShareLinkResult> result = shareLinkService.getUserShareList(request);
        return AjaxJson.getPageData(result.getTotal(), result.getRecords());
    }

    @ApiOperationSupport(order = 8)
    @Operation(summary = "清理过期分享", description = "删除所有已过期的分享链接")
    @DeleteMapping("/expired")
    @SaCheckLogin
    public AjaxJson<Integer> deleteExpiredShares() {
        Integer currentUserId = ZFileAuthUtil.getCurrentUserId();
        int deletedCount = shareLinkService.deleteExpiredLinksByUserId(currentUserId);
        return AjaxJson.getSuccessData(deletedCount);
    }
}
