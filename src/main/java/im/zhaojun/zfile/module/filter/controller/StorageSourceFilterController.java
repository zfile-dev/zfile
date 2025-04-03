package im.zhaojun.zfile.module.filter.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.filter.model.entity.FilterConfig;
import im.zhaojun.zfile.module.filter.service.FilterConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 存储源过滤器维护接口
 *
 * @author zhaojun
 */
@Tag(name = "存储源模块-过滤文件")
@ApiSort(6)
@RestController
@RequestMapping("/admin")
public class StorageSourceFilterController {

    @Resource
    private FilterConfigService filterConfigService;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取存储源过滤文件列表", description ="根据存储源 ID 获取存储源设置的过滤文件列表")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @GetMapping("/storage/{storageId}/filters")
    public AjaxJson<List<FilterConfig>> getFilters(@PathVariable Integer storageId) {
        return AjaxJson.getSuccessData(filterConfigService.findByStorageId(storageId));
    }


    @ApiOperationSupport(order = 2)
    @Operation(summary = "保存存储源过滤文件列表", description ="保存指定存储源 ID 设置的过滤文件列表")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @PostMapping("/storage/{storageId}/filters")
    @DemoDisable
    public AjaxJson<Void> saveFilters(@PathVariable Integer storageId, @RequestBody List<FilterConfig> filter) {
        filterConfigService.batchSave(storageId, filter);
        return AjaxJson.getSuccess();
    }

}