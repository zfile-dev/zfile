package im.zhaojun.zfile.common.exception;

/**
 * 无效的存储源异常
 *
 * @author zhaojun
 */
public class InvalidStorageSourceException extends RuntimeException {

	public InvalidStorageSourceException() {
	}

	public InvalidStorageSourceException(String message) {
		super(message);
	}

	public InvalidStorageSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidStorageSourceException(Throwable cause) {
		super(cause);
	}

	public InvalidStorageSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}