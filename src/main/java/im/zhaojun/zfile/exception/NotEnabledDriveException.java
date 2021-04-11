package im.zhaojun.zfile.exception;

/**
 * 未启用的驱动器异常
 */
public class NotEnabledDriveException extends RuntimeException {

    public NotEnabledDriveException() {
    }

    public NotEnabledDriveException(String message) {
        super(message);
    }

    public NotEnabledDriveException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnabledDriveException(Throwable cause) {
        super(cause);
    }

    public NotEnabledDriveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}