package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import lombok.Getter;

/**
 * 访问了禁止访问的存储源文件/目录时抛出此异常.
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#storageSourceFileForbiddenAccessBizException(StorageSourceFileForbiddenAccessBizException)}
 *
 * @author zhaojun
 */
@Getter
public class StorageSourceFileForbiddenAccessBizException extends BizException {

    private final Integer storageId;

    private final String path;

    public StorageSourceFileForbiddenAccessBizException(Integer storageId, String path) {
        super(ErrorCode.BIZ_STORAGE_SOURCE_FILE_FORBIDDEN);
        this.storageId = storageId;
        this.path = path;
    }
}
