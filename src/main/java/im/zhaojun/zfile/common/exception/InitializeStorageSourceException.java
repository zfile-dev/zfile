package im.zhaojun.zfile.common.exception;

/**
 * 存储源初始化异常
 *
 * @author zhaojun
 */
public class InitializeStorageSourceException extends RuntimeException {

    private static final long serialVersionUID = -1920550904063819880L;

    public InitializeStorageSourceException() {
    }

    public InitializeStorageSourceException(String message) {
        super(message);
    }

    public InitializeStorageSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeStorageSourceException(Throwable cause) {
        super(cause);
    }

    public InitializeStorageSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}