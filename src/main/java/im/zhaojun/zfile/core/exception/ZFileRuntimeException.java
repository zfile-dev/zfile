package im.zhaojun.zfile.core.exception;

/**
 * @author zhaojun
 */
public class ZFileRuntimeException extends RuntimeException {
	
	public ZFileRuntimeException(String message) {
		super(message);
	}
	
	public ZFileRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}