package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import lombok.Getter;

/**
 * 对存储源进行非法(未授权)的操作产生的异常
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#storageSourceIllegalOperationBizException(StorageSourceIllegalOperationBizException)}
 *
 * @author zhaojun
 */
@Getter
public class StorageSourceIllegalOperationBizException extends BizException {

    private final Integer storageId;

    private final FileOperatorTypeEnum action;

    public StorageSourceIllegalOperationBizException(Integer storageId, FileOperatorTypeEnum action) {
        super(ErrorCode.BIZ_STORAGE_SOURCE_ILLEGAL_OPERATION);
        this.storageId = storageId;
        this.action = action;
    }
}
