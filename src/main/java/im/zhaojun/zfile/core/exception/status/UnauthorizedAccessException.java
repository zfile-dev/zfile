package im.zhaojun.zfile.core.exception.status;

import im.zhaojun.zfile.core.exception.core.BizException;

/**
 * 禁止访问异常, 表示用户未进行身份认证, 一般返回 401 状态码.
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link im.zhaojun.zfile.core.exception.GlobalExceptionHandler#unauthorizedAccessException}
 *
 * @author zhaojun
 */
public class UnauthorizedAccessException extends BizException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

}