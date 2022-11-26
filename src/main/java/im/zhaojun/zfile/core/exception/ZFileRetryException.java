package im.zhaojun.zfile.core.exception;

/**
 * @author zhaojun
 */
public class ZFileRetryException extends RuntimeException {
	
	public ZFileRetryException() {
	}
	
	public ZFileRetryException(String message) {
		super(message);
	}
	
	public ZFileRetryException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ZFileRetryException(Throwable cause) {
		super(cause);
	}
	
	public ZFileRetryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}