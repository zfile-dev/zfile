package im.zhaojun.zfile.core.exception.file.operator;

import im.zhaojun.zfile.core.exception.StorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import lombok.Getter;

/**
 * 存储源文件操作异常
 * @author zhaojun
 */
@Getter
public class StorageSourceFileOperatorException extends StorageSourceException {
	
	public StorageSourceFileOperatorException(CodeMsg codeMsg, Integer storageId, String message, Throwable cause) {
		super(codeMsg, storageId, message, cause);
	}
	
}