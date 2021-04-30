package im.zhaojun.zfile.exception;

/**
 * 不存在的文件异常
 * @author zhaojun
 */
public class NotExistFileException extends RuntimeException {

    public NotExistFileException() {
        super();
    }

    public NotExistFileException(String message) {
        super(message);
    }

    public NotExistFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExistFileException(Throwable cause) {
        super(cause);
    }

    protected NotExistFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
