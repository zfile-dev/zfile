package im.zhaojun.zfile.module.storage.function;

import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;

import java.util.function.Function;

/**
 * @author zhaojun
 */
public class SearchFileOperatorTypeEnumDefaultValueFunc implements Function<Integer, FileOperatorTypeDefaultValueDTO> {
	
	private StorageSourceService storageSourceService;
	
	@Override
	public FileOperatorTypeDefaultValueDTO apply(Integer storageId) {
		if (storageSourceService == null) {
			storageSourceService = SpringUtil.getBean(StorageSourceService.class);
		}
		
		StorageSource storageSource = storageSourceService.findById(storageId);
		return new FileOperatorTypeDefaultValueDTO(storageSource.getSearchEnable(), false);
	}
	
}