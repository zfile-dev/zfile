package im.zhaojun.zfile.module.readme.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.module.readme.model.entity.ReadmeConfig;
import im.zhaojun.zfile.module.readme.service.ReadmeConfigService;
import im.zhaojun.zfile.core.util.AjaxJson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 存储源文档模块维护接口
 *
 * @author zhaojun
 */
@Api(tags = "存储源模块-README")
@ApiSort(7)
@RestController
@RequestMapping("/admin")
public class StorageSourceReadmeController {

    @Resource
    private ReadmeConfigService readmeConfigService;

    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取存储源文档文件夹列表", notes = "根据存储源 ID 获取存储源设置的文档文件夹列表")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true, dataTypeClass = Integer.class)
    @GetMapping("/storage/{storageId}/readme")
    public AjaxJson<List<ReadmeConfig>> getReadmeList(@PathVariable Integer storageId) {
        return AjaxJson.getSuccessData(readmeConfigService.findByStorageId(storageId));
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "保存存储源文档文件夹列表", notes = "保存指定存储源 ID 设置的文档文件夹列表")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true, dataTypeClass = Integer.class)
    @PostMapping("/storage/{storageId}/readme")
    public AjaxJson<Void> saveReadmeList(@PathVariable Integer storageId, @RequestBody List<ReadmeConfig> readme) {
        readmeConfigService.batchSave(storageId, readme);
        return AjaxJson.getSuccess();
    }

}