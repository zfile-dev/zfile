package im.zhaojun.zfile.module.storage.function;

import org.apache.commons.lang3.BooleanUtils;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;

import java.util.function.Function;

/**
 * 根据存储源是否允许文件操作来获取权限默认值.
 *
 * @see FileOperatorTypeEnum#NEW_FOLDER
 * @see FileOperatorTypeEnum#DELETE
 * ...
 *
 * @author zhaojun
 */
public class BasicFileOperatorTypeEnumDefaultValueFunc implements Function<Integer, FileOperatorTypeDefaultValueDTO> {
	
	private static StorageSourceService storageSourceService;
	
	/**
	 * 取存储源是否允许文件操作和是否允许文件匿名操作.
	 * 		如果允许文件操作, 则管理员有权限
	 * 		如果允许文件操作, 且允许文件匿名操作, 则匿名用户有权限.
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @return	文件操作类型默认值
	 */
	@Override
	public FileOperatorTypeDefaultValueDTO apply(Integer storageId) {
		if (storageSourceService == null) {
			storageSourceService = SpringUtil.getBean(StorageSourceService.class);
		}
		StorageSource storageSource = storageSourceService.findById(storageId);
		Boolean enableFileOperator = BooleanUtils.isTrue(storageSource.getEnableFileOperator());
		Boolean enableFileAnnoOperator = BooleanUtils.isTrue(storageSource.getEnableFileAnnoOperator());
		
		boolean allowAdmin = enableFileOperator;
		boolean allowAnonymous = enableFileOperator && enableFileAnnoOperator;
		return new FileOperatorTypeDefaultValueDTO(allowAdmin, allowAnonymous);
	}
	
	
}