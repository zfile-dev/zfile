package im.zhaojun.zfile.core.exception.biz;


import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import lombok.Getter;

/**
 * 文件路径安全异常, 表示文件路径不合法，如包含了 "./" 或 "../" 等字符来尝试访问非法目录.
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#filePathSecurityBizException(FilePathSecurityBizException)}
 *
 * @author zhaojun
 */
@Getter
public class FilePathSecurityBizException extends BizException {

    private final String path;

    public FilePathSecurityBizException(String path) {
        super(ErrorCode.BIZ_FILE_PATH_ILLEGAL);
        this.path = path;
    }

}