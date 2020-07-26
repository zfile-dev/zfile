package im.zhaojun.zfile.exception;

/**
 * 对象存储初始化异常
 * @author zhaojun
 */
public class InitializeDriveException extends RuntimeException {

    private static final long serialVersionUID = -1920550904063819880L;

    public InitializeDriveException() {
    }

    public InitializeDriveException(String message) {
        super(message);
    }

    public InitializeDriveException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeDriveException(Throwable cause) {
        super(cause);
    }

    public InitializeDriveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
