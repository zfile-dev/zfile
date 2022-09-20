package im.zhaojun.zfile.core.exception;

import im.zhaojun.zfile.core.util.CodeMsg;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 存储源异常
 *
 * @author zhaojun
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class StorageSourceException extends ServiceException {
	
	/**
	 * 是否使用异常消息进行接口返回，如果是则取异常的 message, 否则取 CodeMsg 中的 message
	 */
	private boolean responseExceptionMessage;
	
	/**
	 * 存储源 ID
	 */
	private final Integer storageId;
	
	public StorageSourceException(CodeMsg codeMsg, Integer storageId, String message) {
		super(message, codeMsg);
		this.storageId = storageId;
	}
	
	public StorageSourceException(CodeMsg codeMsg, Integer storageId, String message, Throwable cause) {
		super(message, cause, codeMsg);
		this.storageId = storageId;
	}
	
	
	/**
	 * 根据 responseExceptionMessage 判断使用异常消息进行接口返回，如果是则取异常的 message, 否则取 CodeMsg 中的 message
	 *
	 * @return		异常消息
	 */
	public String getResultMessage() {
		return responseExceptionMessage ? super.getMessage() : super.getCodeMsg().getMsg();
	}
	
	
	/**
	 * 设置值是否使用异常消息进行接口返回
	 *
	 * @param 	responseExceptionMessage
	 * 			是否使用异常消息进行接口返回
	 *
	 * @return	当前对象
	 */
	public StorageSourceException setResponseExceptionMessage(boolean responseExceptionMessage) {
		this.responseExceptionMessage = responseExceptionMessage;
		return this;
	}
	
}