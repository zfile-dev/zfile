package im.zhaojun.zfile.home.controller;

import cn.hutool.core.util.BooleanUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.admin.model.entity.ReadmeConfig;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.enums.ReadmeDisplayModeEnum;
import im.zhaojun.zfile.admin.model.param.IStorageParam;
import im.zhaojun.zfile.admin.service.ReadmeConfigService;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.admin.service.SystemConfigService;
import im.zhaojun.zfile.common.config.ZFileProperties;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.common.exception.InvalidStorageSourceException;
import im.zhaojun.zfile.common.exception.NotExistFileException;
import im.zhaojun.zfile.common.util.AjaxJson;
import im.zhaojun.zfile.common.util.HttpUtil;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.convert.StorageSourceConvert;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.home.model.request.FileListConfigRequest;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import im.zhaojun.zfile.home.model.result.SiteConfigResult;
import im.zhaojun.zfile.home.model.result.StorageSourceConfigResult;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
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
	private StorageSourceConvert storageSourceConvert;

	@Resource
	private StorageSourceService storageSourceService;

	@Resource
	private SystemConfigService systemConfigService;

	@Resource
	private ReadmeConfigService readmeConfigService;
	
	@Resource
	private StorageSourceContext storageSourceContext;


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

		String storageKey = fileListConfigRequest.getStorageKey();
		String path = fileListConfigRequest.getPath();

		StorageSource storageSource = storageSourceService.findByStorageKey(storageKey);
		if (storageSource == null) {
			throw new InvalidStorageSourceException("存储源不存在");
		}

		StorageSourceConfigResult storageSourceConfigResult = storageSourceConvert.entityToConfigResult(storageSource);

		// 获取是否允许文件操作
		storageSourceConfigResult.setEnableFileOperator(storageSource.allowOperator());

		// 根据存储源 key 获取存储源 id
		Integer storageId = storageSource.getId();
		
		
		ReadmeConfig readmeByPath = new ReadmeConfig();
		readmeByPath.setStorageId(storageId);
		readmeByPath.setDisplayMode(ReadmeDisplayModeEnum.BOTTOM);
		if (BooleanUtil.isTrue(storageSource.getCompatibilityReadme())) {
			try {
				log.info("存储源 {} 兼容获取目录 {} 下的 readme.md", storageSource.getName(), path);
				AbstractBaseFileService<IStorageParam> abstractBaseFileService = storageSourceContext.get(storageId);
				String pathAndName = StringUtils.concat(path, "readme.md");
				FileItemResult fileItem = abstractBaseFileService.getFileItem(pathAndName);
				if (fileItem != null) {
					String url = fileItem.getUrl();
					String readmeText = HttpUtil.getTextContent(url);
					readmeByPath.setReadmeText(readmeText);
				}
			} catch (Exception e) {
				if (e instanceof NotExistFileException) {
					log.error("存储源 {} 兼容获取目录 {} 下的 readme.md 文件失败", storageSource.getName(), path);
				} else {
					log.error("存储源 {} 兼容获取目录 {} 下的 readme.md 文件失败", storageSource.getName(), path, e);
				}
			}
		} else {
			// 获取指定目录 readme 文件
			ReadmeConfig dbReadmeConfig = readmeConfigService.findReadmeByPath(storageId, path);
			if (dbReadmeConfig != null) {
				readmeByPath = dbReadmeConfig;
			}
			log.info("存储源 {} 规则模式获取目录 {} 下文档信息", storageSource.getName(), path);
		}
		
		storageSourceConfigResult.setReadmeDisplayMode(readmeByPath.getDisplayMode());
		storageSourceConfigResult.setReadmeText(readmeByPath.getReadmeText());
		
		return AjaxJson.getSuccessData(storageSourceConfigResult);
	}


	@ResponseBody
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "重置管理员密码", notes = "开启 debug 模式时，访问此接口会强制将管理员账户密码修改为 admin 123456, 并修改登录验证方式为图片验证码, 详见：https://docs.zfile.vip/#/question?id=reset-pwd")
	@GetMapping("/reset-password")
	public AjaxJson<?> resetPwd() {
		if (zFileProperties.isDebug()) {
			systemConfigService.resetAdminLoginInfo();
			return AjaxJson.getSuccess();
		} else {
			return AjaxJson.getError("未开启 DEBUG 模式，不允许进行此操作。");
		}
	}

}