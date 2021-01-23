package im.zhaojun.zfile.exception;

/**
 * 文件不允许下载异常
 * @author zhaojun
 */
public class NotAllowedDownloadException extends RuntimeException {
    public NotAllowedDownloadException() {
    }

    public NotAllowedDownloadException(String message) {
        super(message);
    }

    public NotAllowedDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAllowedDownloadException(Throwable cause) {
        super(cause);
    }

    public NotAllowedDownloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
