package im.zhaojun.zfile.module.password.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.password.model.entity.PasswordConfig;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 存储源密码维护接口
 *
 * @author zhaojun
 */
@Tag(name = "存储源模块-密码文件夹")
@ApiSort(6)
@RestController
@RequestMapping("/admin")
public class StorageSourcePasswordController {

    @Resource
    private PasswordConfigService passwordConfigService;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取存储源密码文件夹列表", description ="根据存储源 ID 获取存储源设置的密码文件夹列表")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @GetMapping("/storage/{storageId}/password")
    public AjaxJson<List<PasswordConfig>> getPasswordList(@PathVariable Integer storageId) {
        return AjaxJson.getSuccessData(passwordConfigService.findByStorageId(storageId));
    }


    @ApiOperationSupport(order = 2)
    @Operation(summary = "保存存储源密码文件夹列表", description ="保存指定存储源 ID 设置的密码文件夹列表")
    @Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
    @PostMapping("/storage/{storageId}/password")
    @DemoDisable
    public AjaxJson<Void> savePasswordList(@PathVariable Integer storageId, @RequestBody List<PasswordConfig> password) {
        passwordConfigService.batchSave(storageId, password);
        return AjaxJson.getSuccess();
    }

}