package im.zhaojun.zfile.common.exception.file.operator;

import im.zhaojun.zfile.common.exception.file.StorageSourceException;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件下载异常
 *
 * @author zhaojun
 */
@Getter
@Setter
public class DownloadFileException extends StorageSourceException {

	// 下载文件路径
	private String pathAndName;

	public DownloadFileException(Integer storageId, String pathAndName, Throwable cause) {
		super(storageId, cause);
		this.pathAndName = pathAndName;
	}

}