package im.zhaojun.zfile.home.service.base;

import im.zhaojun.zfile.admin.model.param.ProxyDownloadParam;

import java.io.InputStream;

/**
 * 代理下载 Service, 如果只需要代理下载, 则可实现此抽象类.

 * @author zhaojun
 */
public abstract class ProxyDownloadService<P extends ProxyDownloadParam> extends ProxyTransferService<P> {

	/**
	 * 空实现.
	 */
	@Override
	public void uploadFile(String pathAndName, InputStream inputStream) {
	}

}