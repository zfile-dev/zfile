package im.zhaojun.zfile.common.exception;

/**
 * 不支持的操作异常
 *
 * @author zhaojun
 */
public class UnSupportedOperation extends RuntimeException {

    public UnSupportedOperation() {
        super();
    }

    public UnSupportedOperation(String message) {
        super(message);
    }

    public UnSupportedOperation(String message, Throwable cause) {
        super(message, cause);
    }

    public UnSupportedOperation(Throwable cause) {
        super(cause);
    }

    protected UnSupportedOperation(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}