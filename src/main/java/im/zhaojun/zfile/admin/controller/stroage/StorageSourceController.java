package im.zhaojun.zfile.admin.controller.stroage;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.request.SaveStorageSourceRequest;
import im.zhaojun.zfile.admin.model.result.storage.StorageSourceAdminResult;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.common.cache.RefreshTokenCache;
import im.zhaojun.zfile.common.util.AjaxJson;
import im.zhaojun.zfile.home.convert.StorageSourceConvert;
import im.zhaojun.zfile.home.model.dto.StorageSourceDTO;
import im.zhaojun.zfile.home.model.request.UpdateStorageSortRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 存储源基础设置模块接口
 *
 * @author zhaojun
 */
@Api(tags = "存储源模块-基础")
@ApiSort(3)
@RestController
@RequestMapping("/admin")
public class StorageSourceController {

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private StorageSourceConvert storageSourceConvert;


    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取所有存储源列表", notes = "获取所有添加的存储源列表，按照排序值由小到大排序")
    @GetMapping("/storages")
    public AjaxJson<List<StorageSourceAdminResult>> storageList() {
        List<StorageSource> list = storageSourceService.findAllOrderByOrderNum();

        List<StorageSourceAdminResult> storageSourceAdminResults = storageSourceConvert.entityToAdminResultList(list);

        storageSourceAdminResults.forEach(storageSourceAdminResult -> {
            RefreshTokenCache.RefreshTokenInfo refreshTokenInfo = RefreshTokenCache.getRefreshTokenInfo(storageSourceAdminResult.getId());
            storageSourceAdminResult.setRefreshTokenInfo(refreshTokenInfo);
        });

        return AjaxJson.getSuccessData(storageSourceAdminResults);
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "获取指定存储源参数", notes = "获取指定存储源基本信息及其参数")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @GetMapping("/storage/{storageId}")
    public AjaxJson<StorageSourceDTO> storageItem(@PathVariable Integer storageId) {
        StorageSourceDTO storageSourceDTO = storageSourceService.findStorageSourceDTOById(storageId);
        return AjaxJson.getSuccessData(storageSourceDTO);
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "保存存储源参数", notes = "保存存储源的所有参数")
    @PostMapping("/storage")
    public AjaxJson<Void> saveStorageItem(@RequestBody SaveStorageSourceRequest saveStorageSourceRequest) {
        storageSourceService.saveStorageSource(saveStorageSourceRequest);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "删除存储源", notes = "删除存储源基本设置和拓展设置")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @DeleteMapping("/storage/{storageId}")
    public AjaxJson<Void> deleteStorageItem(@PathVariable Integer storageId) {
        storageSourceService.deleteById(storageId);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "启用存储源", notes = "开启存储源后可在前台显示")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/storage/{storageId}/enable")
    public AjaxJson<Void> enable(@PathVariable Integer storageId) {
        StorageSource storageSource = storageSourceService.findById(storageId);
        storageSource.setEnable(true);
        storageSourceService.updateById(storageSource);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "停止存储源", notes = "停用存储源后不在前台显示")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true)
    @PostMapping("/storage/{storageId}/disable")
    public AjaxJson<Void> disable(@PathVariable Integer storageId) {
        StorageSource storageSource = storageSourceService.findById(storageId);
        storageSource.setEnable(false);
        storageSourceService.updateById(storageSource);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 7)
    @ApiOperation(value = "更新存储源顺序")
    @PostMapping("/storage/sort")
    public AjaxJson<Void> updateStorageSort(@RequestBody List<UpdateStorageSortRequest> updateStorageSortRequestList) {
        storageSourceService.updateStorageSort(updateStorageSortRequestList);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 8)
    @ApiOperation(value = "校验存储源 key 是否重复")
    @ApiImplicitParam(paramType = "query", name = "storageKey", value = "存储源 key", required = true)
    @GetMapping("/storage/exist/key")
    public AjaxJson<Boolean> existKey(String storageKey) {
        boolean exist = storageSourceService.existByStorageKey(storageKey);
        return AjaxJson.getSuccessData(exist);
    }

}