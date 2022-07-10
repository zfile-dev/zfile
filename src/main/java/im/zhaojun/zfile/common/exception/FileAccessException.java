package im.zhaojun.zfile.common.exception;

/**
 * 文件权限异常
 *
 * @author zhaojun
 */
public class FileAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileAccessException() {
	}

	public FileAccessException(String message) {
		super(message);
	}

	public FileAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileAccessException(Throwable cause) {
		super(cause);
	}

	public FileAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}