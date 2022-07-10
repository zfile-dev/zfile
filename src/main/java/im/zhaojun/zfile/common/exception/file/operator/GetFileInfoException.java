package im.zhaojun.zfile.common.exception.file.operator;

import im.zhaojun.zfile.common.exception.file.StorageSourceException;
import lombok.Getter;
import lombok.Setter;

/**
 * 获取文件信息异常
 *
 * @author zhaojun
 */
@Getter
@Setter
public class GetFileInfoException extends StorageSourceException {

	// 文件信息路径
	private String pathAndName;

	public GetFileInfoException(Integer storageId, String pathAndName) {
		super(storageId);
		this.pathAndName = pathAndName;
	}

	public GetFileInfoException(Integer storageId, String pathAndName, String message) {
		super(storageId, message);
		this.pathAndName = pathAndName;
	}


	public GetFileInfoException(Integer storageId, String pathAndName, Throwable cause) {
		super(storageId, cause);
		this.pathAndName = pathAndName;
	}

}