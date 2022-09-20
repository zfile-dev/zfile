package im.zhaojun.zfile.core.exception;

import im.zhaojun.zfile.core.util.CodeMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Service 层异常
 * 所有 message 均为系统日志打印输出, CodeMsg 中的消息才是返回给客户端的消息.
 *
 * @author zhaojun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {
	
	private CodeMsg codeMsg;
	
	public ServiceException(CodeMsg codeMsg) {
		this.codeMsg = codeMsg;
	}
	
	public ServiceException(String message, CodeMsg codeMsg) {
		super(message);
		this.codeMsg = codeMsg;
	}
	
	public ServiceException(String message, Throwable cause, CodeMsg codeMsg) {
		super(message, cause);
		this.codeMsg = codeMsg;
	}
	
	public ServiceException(Throwable cause, CodeMsg codeMsg) {
		super(cause);
		this.codeMsg = codeMsg;
	}
	
	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, CodeMsg codeMsg) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.codeMsg = codeMsg;
	}
}