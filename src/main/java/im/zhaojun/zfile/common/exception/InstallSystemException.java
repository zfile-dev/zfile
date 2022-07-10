package im.zhaojun.zfile.common.exception;

/**
 * 系统初始化异常
 *
 * @author zhaojun
 */
public class InstallSystemException extends RuntimeException {

    public InstallSystemException() {
        super();
    }

    public InstallSystemException(String message) {
        super(message);
    }

    public InstallSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstallSystemException(Throwable cause) {
        super(cause);
    }

    protected InstallSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}