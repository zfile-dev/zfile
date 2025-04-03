package im.zhaojun.zfile.core.exception.status;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;

/**
 * 访问内容不存在异常, 表示用户请求的资源不存在时抛出, 一般返回 404 状态码.
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link im.zhaojun.zfile.core.exception.GlobalExceptionHandler#notFoundAccessException}
 *
 * @author zhaojun
 */
public class NotFoundAccessException extends BizException {

    public NotFoundAccessException(ErrorCode errorCode) {
        super(errorCode);
    }

}