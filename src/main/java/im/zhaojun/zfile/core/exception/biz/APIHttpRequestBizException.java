package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import lombok.Getter;

/**
 * 请求第三方 API 时如果返回非 2xx 状态码, 则抛出此异常. 需记录请求地址, 响应状态码, 响应内容.
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#apiHttpRequestBizException(APIHttpRequestBizException)}
 *
 * @author zhaojun
 */
@Getter
public class APIHttpRequestBizException extends BizException {

    private final String url;

    private final int responseCode;

    private final String responseBody;

    public APIHttpRequestBizException(ErrorCode errorCode, String url, int responseCode, String responseBody) {
        super(errorCode);
        this.url = url;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

}
