package im.zhaojun.zfile.module.config.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.storage.model.request.base.FileListConfigRequest;
import im.zhaojun.zfile.module.config.model.result.SiteConfigResult;
import im.zhaojun.zfile.module.storage.model.result.StorageSourceConfigResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 站点基础模块接口
 *
 * @author zhaojun
 */
@Api(tags = "站点基础模块")
@ApiSort(1)
@Slf4j
@RequestMapping("/api/site")
@RestController
public class SiteController {

	@Resource
	private ZFileProperties zFileProperties;

	@Resource
	private StorageSourceService storageSourceService;

	@Resource
	private SystemConfigService systemConfigService;

	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取站点全局设置", notes = "获取站点全局设置, 包括是否页面布局、列表尺寸、公告、配置信息")
	@GetMapping("/config/global")
	public AjaxJson<SiteConfigResult> globalConfig() {
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();

		SiteConfigResult siteConfigResult = new SiteConfigResult();
		BeanUtils.copyProperties(systemConfig, siteConfigResult);

		siteConfigResult.setDebugMode(zFileProperties.isDebug());
		return AjaxJson.getSuccessData(siteConfigResult);
	}


	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "获取存储源设置", notes = "获取某个存储源的设置信息, 包括是否启用, 名称, 存储源类型, 存储源配置信息")
	@PostMapping("/config/storage")
	public AjaxJson<StorageSourceConfigResult> storageList(@Valid @RequestBody FileListConfigRequest fileListConfigRequest) {
		StorageSourceConfigResult storageSourceConfigResult = storageSourceService.getStorageConfigSource(fileListConfigRequest);
		return AjaxJson.getSuccessData(storageSourceConfigResult);
	}


	@ResponseBody
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "重置管理员密码", notes = "开启 debug 模式时，访问此接口会强制将管理员账户密码修改为 admin 123456, 并修改登录验证方式为图片验证码, 详见：https://docs.zfile.vip/#/question?id=reset-pwd")
	@GetMapping("/reset-password")
	public AjaxJson<?> resetPwd() {
		systemConfigService.resetAdminLoginInfo();
		return AjaxJson.getSuccess();
	}

}