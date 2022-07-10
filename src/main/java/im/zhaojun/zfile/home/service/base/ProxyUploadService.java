package im.zhaojun.zfile.home.service.base;

import im.zhaojun.zfile.admin.model.param.ProxyUploadParam;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

/**
 * 代理上传 Service, 如果只需要代理上传, 则可实现此抽象类.
 *
 * @author zhaojun
 */
public abstract class ProxyUploadService<P extends ProxyUploadParam> extends ProxyTransferService<P> {

	/**
	 * 空实现.
	 */
	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) {
		return null;
	}

}