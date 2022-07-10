package im.zhaojun.zfile.common.exception;

/**
 * 存储源不支持代理上传异常
 *
 * @author zhaojun
 */
public class StorageSourceNotSupportProxyUploadException extends RuntimeException {

	public StorageSourceNotSupportProxyUploadException(String message) {
		super(message);
	}

}