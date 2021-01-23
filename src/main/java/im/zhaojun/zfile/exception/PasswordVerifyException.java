package im.zhaojun.zfile.exception;

/**
 * 密码校验失败异常
 * @author zhaojun
 */
public class PasswordVerifyException extends RuntimeException {

    public PasswordVerifyException() {
    }

    public PasswordVerifyException(String message) {
        super(message);
    }

    public PasswordVerifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordVerifyException(Throwable cause) {
        super(cause);
    }

    public PasswordVerifyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}