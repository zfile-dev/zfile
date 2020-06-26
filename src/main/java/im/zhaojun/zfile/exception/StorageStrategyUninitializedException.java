package im.zhaojun.zfile.exception;

/**
 * 存储策略未初始化异常
 * @author zhaojun
 */
public class StorageStrategyUninitializedException extends RuntimeException {

    private static final long serialVersionUID = 5736940575583615661L;

    public StorageStrategyUninitializedException() {
    }

    public StorageStrategyUninitializedException(String message) {
        super(message);
    }

    public StorageStrategyUninitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageStrategyUninitializedException(Throwable cause) {
        super(cause);
    }

    public StorageStrategyUninitializedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}