package im.zhaojun.zfile.module.storage.function;

import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;

import java.util.function.Function;

/**
 * 默认允许管理员操作.
 *
 * @author zhaojun
 */
public class AllowAdminFileOperatorTypeEnumDefaultValueFunc implements Function<Integer, FileOperatorTypeDefaultValueDTO> {
	
	/**
	 * 默认允许管理员操作
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @return	文件操作类型默认值
	 */
	@Override
	public FileOperatorTypeDefaultValueDTO apply(Integer storageId) {
		return new FileOperatorTypeDefaultValueDTO(true, false);
	}
	
}