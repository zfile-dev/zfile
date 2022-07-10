package im.zhaojun.zfile.admin.exception;

import lombok.Getter;

/**
 * 禁止的文件操作异常
 *
 * @author zhaojun
 */
@Getter
public class ForbidFileOperationException extends RuntimeException {

	private final Integer storageId;

	private final String action;

	public ForbidFileOperationException(Integer storageId, String action) {
		this.storageId = storageId;
		this.action = action;
	}

}