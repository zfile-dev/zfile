package im.zhaojun.zfile.exception;

/**
 * 未知的存储类型异常
 * @author zhaojun
 */
public class UnknownStorageTypeException extends RuntimeException {

    private static final long serialVersionUID = -4853756482605773655L;

    public UnknownStorageTypeException() {
    }

    public UnknownStorageTypeException(String message) {
        super(message);
    }

    public UnknownStorageTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownStorageTypeException(Throwable cause) {
        super(cause);
    }

    public UnknownStorageTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}