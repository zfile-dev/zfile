package im.zhaojun.zfile.core.exception.biz;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import lombok.Getter;

/**
 * 不存在或初始化失败的存储源异常。
 *
 * @author zhaojun
 */
@Getter
public class InvalidStorageSourceBizException extends BizException {

	private final Integer storageId;

	private final String storageKey;

	public InvalidStorageSourceBizException(String storageKey) {
		super(ErrorCode.INVALID_STORAGE_SOURCE);
		this.storageKey = storageKey;
		this.storageId = null;
	}

	public InvalidStorageSourceBizException(Integer storageId) {
		super(ErrorCode.INVALID_STORAGE_SOURCE);
		this.storageId = storageId;
		this.storageKey = null;
    }

}