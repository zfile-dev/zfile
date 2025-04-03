package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import lombok.Getter;

/**
 * 获取预览文件内容异常, 可能是目标连接无法访问/文件不存在等原因.
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#getPreviewTextContentBizException(GetPreviewTextContentBizException)}
 *
 * @author zhaojun
 */
@Getter
public class GetPreviewTextContentBizException extends BizException {

    /**
     * 获取预览文件的 URL
     */
    private final String url;

    public GetPreviewTextContentBizException(String url, Throwable cause) {
        super(cause);
        this.url = url;
    }

}