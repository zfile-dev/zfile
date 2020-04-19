package im.zhaojun.zfile.exception;

/**
 * 文件预览异常类
 * @author zhaojun
 */
public class PreviewException extends RuntimeException {

    public PreviewException() {
    }

    public PreviewException(String message) {
        super(message);
    }

    public PreviewException(String message, Throwable cause) {
        super(message, cause);
    }

    public PreviewException(Throwable cause) {
        super(cause);
    }

    public PreviewException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}