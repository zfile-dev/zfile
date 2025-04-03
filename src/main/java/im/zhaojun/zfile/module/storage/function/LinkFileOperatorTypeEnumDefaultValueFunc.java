package im.zhaojun.zfile.module.storage.function;

import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;

import java.util.function.Function;

/**
 * 根据全局站点设置是否允许使用直链控制权限.
 *
 * @author zhaojun
 */
public class LinkFileOperatorTypeEnumDefaultValueFunc implements Function<Integer, FileOperatorTypeDefaultValueDTO> {
	
	private static SystemConfigService systemConfigService;
	
	/**
	 * 根据全局站点设置是否允许使用直链控制权限.
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @return	文件操作类型默认值
	 */
	@Override
	public FileOperatorTypeDefaultValueDTO apply(Integer storageId) {
		if (systemConfigService == null) {
			systemConfigService = SpringUtil.getBean(SystemConfigService.class);
		}
		
		SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
		
		Boolean showPathLink = systemConfig.getShowPathLink();
		return new FileOperatorTypeDefaultValueDTO(showPathLink, showPathLink);
	}
	
	
}