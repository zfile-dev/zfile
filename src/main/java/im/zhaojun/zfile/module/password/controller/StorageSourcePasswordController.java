package im.zhaojun.zfile.module.password.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.module.password.model.entity.PasswordConfig;
import im.zhaojun.zfile.module.password.service.PasswordConfigService;
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
 * 存储源密码维护接口
 *
 * @author zhaojun
 */
@Api(tags = "存储源模块-密码文件夹")
@ApiSort(6)
@RestController
@RequestMapping("/admin")
public class StorageSourcePasswordController {

    @Resource
    private PasswordConfigService passwordConfigService;

    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取存储源密码文件夹列表", notes = "根据存储源 ID 获取存储源设置的密码文件夹列表")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true, dataTypeClass = Integer.class)
    @GetMapping("/storage/{storageId}/password")
    public AjaxJson<List<PasswordConfig>> getPasswordList(@PathVariable Integer storageId) {
        return AjaxJson.getSuccessData(passwordConfigService.findByStorageId(storageId));
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "保存存储源密码文件夹列表", notes = "保存指定存储源 ID 设置的密码文件夹列表")
    @ApiImplicitParam(paramType = "path", name = "storageId", value = "存储源 id", required = true, dataTypeClass = Integer.class)
    @PostMapping("/storage/{storageId}/password")
    public AjaxJson<Void> savePasswordList(@PathVariable Integer storageId, @RequestBody List<PasswordConfig> password) {
        passwordConfigService.batchSave(storageId, password);
        return AjaxJson.getSuccess();
    }

}