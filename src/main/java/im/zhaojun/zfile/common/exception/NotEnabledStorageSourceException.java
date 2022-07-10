package im.zhaojun.zfile.common.exception;

/**
 * 未启用的存储源异常
 *
 * @author zhaojun
 */
public class NotEnabledStorageSourceException extends RuntimeException {

    public NotEnabledStorageSourceException() {
    }

    public NotEnabledStorageSourceException(String message) {
        super(message);
    }

    public NotEnabledStorageSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnabledStorageSourceException(Throwable cause) {
        super(cause);
    }

    public NotEnabledStorageSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}