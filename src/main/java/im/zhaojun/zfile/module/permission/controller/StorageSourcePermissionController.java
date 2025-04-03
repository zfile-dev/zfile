package im.zhaojun.zfile.module.permission.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.permission.convert.PermissionConfigConvert;
import im.zhaojun.zfile.module.permission.model.entity.PermissionConfig;
import im.zhaojun.zfile.module.permission.model.result.PermissionConfigResult;
import im.zhaojun.zfile.module.permission.service.PermissionConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 存储源权限控制 Controller
 *
 * @author zhaojun
 */
@Tag(name = "存储源模块-权限控制")
@ApiSort(6)
@RestController
@RequestMapping("/admin")
public class StorageSourcePermissionController {

	@Resource
	private PermissionConfigService permissionConfigService;

	@Resource
	private PermissionConfigConvert permissionConfigConvert;

	@ApiOperationSupport(order = 1)
	@Operation(summary = "获取存储源权限列表", description ="根据存储源 ID 获取存储源权限列表")
	@Parameter(in = ParameterIn.PATH, name = "storageId", description = "存储源 id", required = true, schema = @Schema(type = "integer"))
	@GetMapping("/storage/{storageId}/permission")
	public AjaxJson<List<PermissionConfigResult>> getPermissionList(@PathVariable Integer storageId) {
		List<PermissionConfig> permissionList = permissionConfigService.findByStorageId(storageId);

		List<PermissionConfigResult> permissionConfigResults = permissionConfigConvert.toResult(permissionList);
		permissionConfigResults.forEach(permissionConfigResult -> {
			permissionConfigResult.setOperatorName(permissionConfigResult.getOperator().getName());
			permissionConfigResult.setTips(permissionConfigResult.getOperator().getTips());
		});
		
		return AjaxJson.getSuccessData(permissionConfigResults);
	}

}