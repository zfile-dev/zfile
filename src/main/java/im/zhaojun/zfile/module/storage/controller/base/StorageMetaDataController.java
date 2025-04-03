package im.zhaojun.zfile.module.storage.controller.base;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.core.util.AjaxJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统元数据接口
 *
 * @author zhaojun
 */
@Tag(name = "存储源模块-元数据")
@ApiSort(4)
@RestController
@RequestMapping("/admin")
public class StorageMetaDataController {

    @GetMapping("/support-storage")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取支持的存储源类型", description = "获取系统支持的存储源类型")
    public AjaxJson<StorageTypeEnum[]> supportStorage() {
        return AjaxJson.getSuccessData(StorageTypeEnum.values());
    }


    @GetMapping("/storage-params")
    @ApiOperationSupport(order = 2)
    @Operation(summary = "获取指定存储源类型的所有参数信息", description = "获取指定存储源类型的参数，如本地存储只需要填路径地址，而对象存储需要填 AccessKey, SecretKey 等信息.")
    public AjaxJson<List<StorageSourceParamDef>> getFormByStorageType(StorageTypeEnum storageType) {
        List<StorageSourceParamDef> storageSourceConfigList = StorageSourceContext.getStorageSourceParamListByType(storageType);
        return AjaxJson.getSuccessData(storageSourceConfigList);
    }

}