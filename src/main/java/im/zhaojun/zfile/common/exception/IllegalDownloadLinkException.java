package im.zhaojun.zfile.common.exception;

/**
 * 非法使用下载链接异常.
 *
 * @author zhaojun
 */
public class IllegalDownloadLinkException extends RuntimeException {

	public IllegalDownloadLinkException() {
		super();
	}

	public IllegalDownloadLinkException(String message) {
		super(message);
	}

	public IllegalDownloadLinkException(String message, Throwable cause) {
		super(message, cause);
	}
}