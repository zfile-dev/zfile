package im.zhaojun.zfile.module.share.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.share.model.request.ShareLinkListRequest;
import im.zhaojun.zfile.module.share.model.result.ShareLinkResult;
import im.zhaojun.zfile.module.share.service.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员分享文件相关接口.
 */
@Tag(name = "分享管理模块")
@ApiSort(31)
@RestController
@RequestMapping("/admin/share")
public class ShareFileManagerController {

    @Resource
    private ShareLinkService shareLinkService;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "分页查询分享列表", description = "管理员查看所有分享记录")
    @GetMapping("/list")
    public AjaxJson<List<ShareLinkResult>> getShareList(@Valid ShareLinkListRequest request) {
        Page<ShareLinkResult> result = shareLinkService.getAdminShareList(request);
        return AjaxJson.getPageData(result.getTotal(), result.getRecords());
    }

    @ApiOperationSupport(order = 2)
    @Operation(summary = "清理过期分享", description = "删除所有已过期的分享记录")
    @DeleteMapping("/expired")
    public AjaxJson<Integer> deleteExpiredShares() {
        int deletedCount = shareLinkService.deleteExpiredLinks();
        return AjaxJson.getSuccessData(deletedCount);
    }
}
