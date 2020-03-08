package im.zhaojun.zfile.exception;

/**
 * @author zhaojun
 */
public class SearchDisableException extends RuntimeException {

    public SearchDisableException() {
    }

    public SearchDisableException(String message) {
        super(message);
    }

    public SearchDisableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchDisableException(Throwable cause) {
        super(cause);
    }

    public SearchDisableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
