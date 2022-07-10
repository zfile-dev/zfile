package im.zhaojun.zfile.common.exception;

import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import lombok.Getter;

/**
 * 文件上传异常
 *
 * @author zhaojun
 */
@Getter
public class FileUploadException extends RuntimeException {

	private final StorageTypeEnum storageTypeEnum;

	private final Integer storageId;

	private final String path;

	public FileUploadException(StorageTypeEnum storageTypeEnum, Integer storageId, String path, Throwable cause) {
		super(cause);
		this.storageTypeEnum = storageTypeEnum;
		this.path = path;
		this.storageId = storageId;
	}

}