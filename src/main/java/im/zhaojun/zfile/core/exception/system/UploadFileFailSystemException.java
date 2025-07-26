package im.zhaojun.zfile.core.exception.system;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.GlobalExceptionHandler;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import lombok.Getter;


/**
 * 上传文件失败系统异常, 该异常用户无法处理，需要记录日志, 属于系统异常. 如: 网络异常, 目标存储源异常等
 * <p/>
 * 需要全局异常处理器捕获此异常, 并记录日志. {@link GlobalExceptionHandler#uploadFileFailSystemException(UploadFileFailSystemException)}
 *
 * @author zhaojun
 */
@Getter
public class UploadFileFailSystemException extends SystemException {

    private final StorageTypeEnum storageTypeEnum;

    private final String uploadPath;

    private final Long inputStreamAvailable;

    private final int responseCode;

    private final String responseBody;

    public UploadFileFailSystemException(StorageTypeEnum storageTypeEnum, String uploadPath, Long inputStreamAvailable, int responseCode, String responseBody) {
        this(storageTypeEnum, uploadPath, inputStreamAvailable, responseCode, responseBody, null);
    }

    public UploadFileFailSystemException(StorageTypeEnum storageTypeEnum, String uploadPath, Long inputStreamAvailable, int responseCode, String responseBody, Throwable cause) {
        super(ErrorCode.BIZ_UPLOAD_FILE_ERROR, cause);
        this.storageTypeEnum = storageTypeEnum;
        this.uploadPath = uploadPath;
        this.inputStreamAvailable = inputStreamAvailable;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

}