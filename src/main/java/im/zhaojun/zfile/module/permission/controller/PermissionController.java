package im.zhaojun.zfile.module.permission.controller;

import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.permission.model.result.PermissionInfoResult;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "权限模块")
@ApiSort(6)
@RestController
@RequestMapping("/admin/permission")
public class PermissionController {

    @GetMapping("list")
    public AjaxJson<List<PermissionInfoResult>> list() {
        FileOperatorTypeEnum[] values = FileOperatorTypeEnum.values();
        List<PermissionInfoResult> permissionInfoResults = new java.util.ArrayList<>(values.length);
        for (FileOperatorTypeEnum value : values) {
            if (value.isDeprecated()) {
                continue;
            }
            PermissionInfoResult permissionInfoResult = new PermissionInfoResult();
            permissionInfoResult.setName(value.getName());
            permissionInfoResult.setValue(value.getValue());
            permissionInfoResult.setTips(value.getTips());
            permissionInfoResults.add(permissionInfoResult);
        }
        return AjaxJson.getSuccessData(permissionInfoResults);
    }

}
