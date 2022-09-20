package im.zhaojun.zfile.core.exception.file;

import im.zhaojun.zfile.core.exception.StorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;

/**
 * 无效的存储源异常
 *
 * @author zhaojun
 */
public class InvalidStorageSourceException extends StorageSourceException {
	
	public InvalidStorageSourceException(String message) {
		super(CodeMsg.STORAGE_SOURCE_NOT_FOUND, null, message);
	}
	
	public InvalidStorageSourceException(Integer storageId) {
		super(CodeMsg.STORAGE_SOURCE_NOT_FOUND, storageId, CodeMsg.STORAGE_SOURCE_NOT_FOUND.getMsg());
	}
	
	public InvalidStorageSourceException(Integer storageId, String message) {
		super(CodeMsg.STORAGE_SOURCE_NOT_FOUND, storageId, message);
	}

}