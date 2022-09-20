package im.zhaojun.zfile.core.exception;

/**
 * 文件解析异常
 *
 * @author zhaojun
 */
public class TextParseException extends ZFileRuntimeException {

    public TextParseException(String message) {
        super(message);
    }
    
    public TextParseException(String message, Throwable cause) {
        super(message, cause);
    }
}