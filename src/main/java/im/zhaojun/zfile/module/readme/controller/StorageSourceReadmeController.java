package im.zhaojun.zfile.module.readme.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.readme.model.entity.ReadmeConfig;
import im.zhaojun.zfile.module.readme.service.ReadmeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 存储源文档模块维护接口
 *
 * @author zhaojun
 */
@Tag(name = "存储源模块-README")
@ApiSort(7)
@RestController
@RequestMapping("/admin")
public class StorageSourceReadmeController {

    @Resource
    private ReadmeConfigService readmeConfigService;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取存储源文档文件夹列表", description ="根据存储源 ID 获取存储源设置的文档文件夹列表")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @GetMapping("/storage/{storageId}/readme")
    public AjaxJson<List<ReadmeConfig>> getReadmeList(@PathVariable Integer storageId) {
        return AjaxJson.getSuccessData(readmeConfigService.findByStorageId(storageId));
    }


    @ApiOperationSupport(order = 2)
    @Operation(summary = "保存存储源文档文件夹列表", description ="保存指定存储源 ID 设置的文档文件夹列表")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @PostMapping("/storage/{storageId}/readme")
    @DemoDisable
    public AjaxJson<Void> saveReadmeList(@PathVariable Integer storageId, @RequestBody List<ReadmeConfig> readme) {
        readmeConfigService.batchSave(storageId, readme);
        return AjaxJson.getSuccess();
    }

}