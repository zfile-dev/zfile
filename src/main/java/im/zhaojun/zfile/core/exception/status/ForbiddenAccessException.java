package im.zhaojun.zfile.core.exception.status;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;

/**
 * 禁止访问异常, 表示用户没有权限访问该资源, 一般返回 403 状态码. (已经有身份，如果没有身份，应该是 UnauthorizedAccessException)
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link im.zhaojun.zfile.core.exception.GlobalExceptionHandler#forbiddenAccessException}
 *
 * @author zhaojun
 */
public class ForbiddenAccessException extends BizException {

    public ForbiddenAccessException(ErrorCode errorCode) {
        super(errorCode);
    }

}