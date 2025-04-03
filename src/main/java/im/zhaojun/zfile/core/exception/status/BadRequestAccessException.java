package im.zhaojun.zfile.core.exception.status;

import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import im.zhaojun.zfile.core.exception.core.BizException;

/**
 * 错误请求异常, 表示请求参数有误或者服务器无法理解, 一般返回 400 状态码
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#badRequestAccessException(BadRequestAccessException)}
 *
 * @author zhaojun
 */
public class BadRequestAccessException extends BizException {

    public BadRequestAccessException(String message) {
        super(message);
    }
}