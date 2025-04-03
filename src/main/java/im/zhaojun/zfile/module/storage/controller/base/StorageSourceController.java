package im.zhaojun.zfile.module.storage.controller.base;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.storage.convert.StorageSourceConvert;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.request.admin.CopyStorageSourceRequest;
import im.zhaojun.zfile.module.storage.model.request.admin.UpdateStorageSortRequest;
import im.zhaojun.zfile.module.storage.model.request.base.SaveStorageSourceRequest;
import im.zhaojun.zfile.module.storage.model.result.StorageSourceAdminResult;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 存储源基础设置模块接口
 *
 * @author zhaojun
 */
@Tag(name = "存储源模块-基础")
@ApiSort(3)
@RestController
@RequestMapping("/admin")
public class StorageSourceController {

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private StorageSourceConvert storageSourceConvert;


    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取所有存储源列表", description ="获取所有添加的存储源列表，按照排序值由小到大排序")
    @GetMapping("/storages")
    public AjaxJson<List<StorageSourceAdminResult>> storageList() {
        List<StorageSource> list = storageSourceService.findAllOrderByOrderNum();

        List<StorageSourceAdminResult> storageSourceAdminResults = storageSourceConvert.entityToAdminResultList(list);

        storageSourceAdminResults.forEach(storageSourceAdminResult -> {
            RefreshTokenCacheBO.RefreshTokenInfo refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageSourceAdminResult.getId());
            storageSourceAdminResult.setRefreshTokenInfo(refreshTokenInfo);
        });

        return AjaxJson.getSuccessData(storageSourceAdminResults);
    }


    @ApiOperationSupport(order = 2)
    @Operation(summary = "获取指定存储源参数", description ="获取指定存储源基本信息及其参数")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @GetMapping("/storage/{storageId}")
    public AjaxJson<StorageSourceDTO> storageItem(@PathVariable Integer storageId) {
        StorageSourceDTO storageSourceDTO = storageSourceService.findDTOById(storageId);
        return AjaxJson.getSuccessData(storageSourceDTO);
    }


    @ApiOperationSupport(order = 3)
    @Operation(summary = "保存存储源参数", description ="保存存储源的所有参数")
    @PostMapping("/storage")
    @DemoDisable
    public AjaxJson<Integer> saveStorageItem(@RequestBody SaveStorageSourceRequest saveStorageSourceRequest) {
        Integer id = storageSourceService.saveStorageSource(saveStorageSourceRequest);
        return AjaxJson.getSuccessData(id);
    }


    @ApiOperationSupport(order = 4)
    @Operation(summary = "删除存储源", description ="删除存储源基本设置和拓展设置")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @DeleteMapping("/storage/{storageId}")
    @DemoDisable
    public AjaxJson<Void> deleteStorageItem(@PathVariable Integer storageId) {
        storageSourceService.deleteById(storageId);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 5)
    @Operation(summary = "启用存储源", description ="开启存储源后可在前台显示")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @PostMapping("/storage/{storageId}/enable")
    @DemoDisable
    public AjaxJson<Void> enable(@PathVariable Integer storageId) {
        StorageSource storageSource = storageSourceService.findById(storageId);
        storageSource.setEnable(true);
        storageSourceService.updateById(storageSource);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 6)
    @Operation(summary = "停止存储源", description ="停用存储源后不在前台显示")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @PostMapping("/storage/{storageId}/disable")
    @DemoDisable
    public AjaxJson<Void> disable(@PathVariable Integer storageId) {
        StorageSource storageSource = storageSourceService.findById(storageId);
        storageSource.setEnable(false);
        storageSourceService.updateById(storageSource);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 7)
    @Operation(summary = "更新存储源顺序")
    @PostMapping("/storage/sort")
    public AjaxJson<Void> updateStorageSort(@RequestBody List<UpdateStorageSortRequest> updateStorageSortRequestList) {
        storageSourceService.updateStorageSort(updateStorageSortRequestList);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 8)
    @Operation(summary = "校验存储源 key 是否重复")
    @Parameter(in = ParameterIn.QUERY, name = "storageKey", description = "存储源 key", required = true, schema = @Schema(type = "string"))
    @GetMapping("/storage/exist/key")
    public AjaxJson<Boolean> existKey(String storageKey) {
        boolean exist = storageSourceService.existByStorageKey(storageKey);
        return AjaxJson.getSuccessData(exist);
    }


    @ApiOperationSupport(order = 9)
    @Operation(summary = "修改 readme 兼容模式", description ="修改 readme 兼容模式是否启用")
    @Parameters({
        @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer")),
        @Parameter(in = ParameterIn.PATH, name = "status", description = "存储源兼容模式状态", required = true, schema = @Schema(type = "boolean"))
    })
    @PostMapping("/storage/{storageId}/compatibility_readme/{status}")
    public AjaxJson<Void> changeCompatibilityReadme(@PathVariable Integer storageId, @PathVariable Boolean status) {
        StorageSource storageSource = storageSourceService.findById(storageId);
        storageSource.setCompatibilityReadme(status);
        storageSourceService.updateById(storageSource);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 10)
    @Operation(summary = "复制存储源", description ="复制存储源配置")
    @PostMapping("/storage/copy")
    @DemoDisable
    public AjaxJson<Integer> copyStorage(@RequestBody @Valid CopyStorageSourceRequest copyStorageSourceRequest) {
        Integer id = storageSourceService.copy(copyStorageSourceRequest);
        return AjaxJson.getSuccessData(id);
    }
}