package im.zhaojun.zfile.module.storage.function;

import im.zhaojun.zfile.module.storage.model.dto.FileOperatorTypeDefaultValueDTO;

import java.util.function.Function;

/**
 * 禁用所有操作.
 *
 * @author zhaojun
 */
public class DisableAllFileOperatorTypeEnumDefaultValueFunc implements Function<Integer, FileOperatorTypeDefaultValueDTO> {
	
	/**
	 * 禁用所有操作.
	 *
	 * @param 	storageId
	 * 			存储源 ID
	 *
	 * @return	文件操作类型默认值
	 */
	@Override
	public FileOperatorTypeDefaultValueDTO apply(Integer storageId) {
		return new FileOperatorTypeDefaultValueDTO(false, false);
	}
	
}