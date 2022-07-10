package im.zhaojun.zfile.common.exception.file;

import lombok.Getter;
import lombok.Setter;

/**
 * 存储源异常
 *
 * @author zhaojun
 */
@Getter
@Setter
public class StorageSourceException extends RuntimeException {

	// 存储源 ID
	private Integer storageId;

	public StorageSourceException(Integer storageId) {
		this.storageId = storageId;
	}

	public StorageSourceException(Integer storageId, String message) {
		super(message);
		this.storageId = storageId;
	}

	public StorageSourceException(Integer storageId, Throwable cause) {
		super(cause);
		this.storageId = storageId;
	}

}