package im.zhaojun.zfile.module.config.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.model.result.FrontSiteConfigResult;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.annotation.ProCheck;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.model.request.base.FileListConfigRequest;
import im.zhaojun.zfile.module.storage.model.result.StorageSourceConfigResult;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.user.model.constant.UserConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 面向前台的站点基础模块接口
 *
 * @author zhaojun
 */
@Tag(name = "站点基础模块")
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
	@Operation(summary = "获取站点全局设置", description = "获取站点全局设置, 包括是否页面布局、列表尺寸、公告、配置信息")
	@GetMapping("/config/global")
	@ProCheck
	public AjaxJson<FrontSiteConfigResult> globalConfig() {
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();

		FrontSiteConfigResult frontSiteConfigResult = new FrontSiteConfigResult();
		BeanUtils.copyProperties(systemConfig, frontSiteConfigResult);

		frontSiteConfigResult.setDebugMode(zFileProperties.isDebug());
		boolean guestUser = Objects.equals(ZFileAuthUtil.getCurrentUserId(), UserConstant.ANONYMOUS_ID);
		boolean guestIndexNotBlank = StringUtils.isNotBlank(systemConfig.getGuestIndexHtml());
		frontSiteConfigResult.setGuest(guestUser && guestIndexNotBlank);
		return AjaxJson.getSuccessData(frontSiteConfigResult);
	}


	@ApiOperationSupport(order = 2)
	@Operation(summary = "获取存储源设置", description = "获取某个存储源的设置信息, 包括是否启用, 名称, 存储源类型, 存储源配置信息")
	@PostMapping("/config/storage")
	public AjaxJson<StorageSourceConfigResult> storageList(@Valid @RequestBody FileListConfigRequest fileListConfigRequest) {
		StorageSourceConfigResult storageSourceConfigResult = storageSourceService.getStorageConfigSource(fileListConfigRequest);
		return AjaxJson.getSuccessData(storageSourceConfigResult);
	}


	@ApiOperationSupport(order = 3)
	@Operation(summary = "获取用户存储源路径", description = "获取用户存储源路径")
	@GetMapping("/config/userRootPath/{storageKey}")
	public AjaxJson<String> getUserRootPath(@PathVariable("storageKey") String storageKey) {
		AbstractBaseFileService<?> baseFileService = StorageSourceContext.getByStorageKey(storageKey);
		if (baseFileService == null || baseFileService.getCurrentUserBasePath() == null) {
			return AjaxJson.getSuccessData("");
		}
		return AjaxJson.getSuccessData(baseFileService.getCurrentUserBasePath());
	}

}