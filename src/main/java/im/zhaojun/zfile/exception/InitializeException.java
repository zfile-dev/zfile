package im.zhaojun.zfile.exception;

/**
 * 对象存储初始化异常
 * @author zhaojun
 */
public class InitializeException extends RuntimeException {

    private static final long serialVersionUID = -1920550904063819880L;

    public InitializeException() {
    }

    public InitializeException(String message) {
        super(message);
    }

    public InitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeException(Throwable cause) {
        super(cause);
    }

    public InitializeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
