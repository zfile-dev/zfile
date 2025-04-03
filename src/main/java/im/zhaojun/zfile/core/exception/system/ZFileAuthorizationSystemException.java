package im.zhaojun.zfile.core.exception.system;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import im.zhaojun.zfile.core.exception.core.SystemException;

/**
 * ZFile 授权异常
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#zfileAuthorizationSystemException(ZFileAuthorizationSystemException)}
 *
 * @author zhaojun
 */
public class ZFileAuthorizationSystemException extends SystemException {

    public ZFileAuthorizationSystemException(String code, String message) {
        super(code, message);
    }

    public ZFileAuthorizationSystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ZFileAuthorizationSystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

}
