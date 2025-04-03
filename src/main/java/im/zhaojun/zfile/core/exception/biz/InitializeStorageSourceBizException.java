package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import im.zhaojun.zfile.core.exception.core.BizException;
import lombok.Getter;

/**
 * 初始化存储源时失败产生的异常
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#initializeStorageSourceBizException(InitializeStorageSourceBizException)}
 *
 * @author zhaojun
 */
@Getter
public class InitializeStorageSourceBizException extends BizException {

    private final Integer storageId;

    public InitializeStorageSourceBizException(String message, Integer storageId) {
        super(message);
        this.storageId = storageId;
    }

    public InitializeStorageSourceBizException(String code, String message, Integer storageId, Throwable cause) {
        super(code, message, cause);
        this.storageId = storageId;
    }

}
