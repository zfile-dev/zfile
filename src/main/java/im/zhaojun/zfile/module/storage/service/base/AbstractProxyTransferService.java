package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.module.storage.model.param.ProxyTransferParam;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.core.util.ProxyDownloadUrlUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;

/**
 * 代理传输数据(上传/下载) Service
 *
 * @author zhaojun
 */
public abstract class AbstractProxyTransferService<P extends ProxyTransferParam> extends AbstractBaseFileService<P>{


	/**
	 * 服务器代理下载 URL 前缀.
	 */
	public static final String PROXY_DOWNLOAD_LINK_PREFIX = "/pd";


	/**
	 * 服务器代理下载 URL 前缀.
	 */
	public static final String PROXY_UPLOAD_LINK_PREFIX = "/file/upload";


	@javax.annotation.Resource
	private SystemConfigService systemConfigService;


	@javax.annotation.Resource
	private StorageSourceService storageSourceService;


	/**
	 * 获取默认代理下载 URL.
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @return  默认的代理下载 URL
	 */
	@Override
	public String getDownloadUrl(String pathAndName) {
		String signature = "";
		if (param.isPrivate()) {
			signature = "?signature=" + ProxyDownloadUrlUtils.generatorSignature(storageId, pathAndName, param.getTokenTime());
		}
		// 如果未填写下载域名，则默认使用带来下载地址.
		if (StrUtil.isEmpty(param.getDomain())) {
			String domain = systemConfigService.getDomain();
			String storageKey = storageSourceService.findStorageKeyById(storageId);
			return StringUtils.concat(domain, PROXY_DOWNLOAD_LINK_PREFIX, storageKey, StringUtils.encodeAllIgnoreSlashes(pathAndName)) + signature;
		} else {
			return StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(pathAndName)) + signature;
		}
	}


	/**
	 * 获取默认代理上传 URL.
	 *
	 * @param   path
	 *          文件路径
	 *
	 * @param   name
	 *          文件名称
	 *
	 * @param   size
	 *          文件大小
	 *
	 * @return  默认的代理下上传 URL
	 */
	@Override
	public String getUploadUrl(String path, String name, Long size) {
		String domain = systemConfigService.getDomain();
		String storageKey = storageSourceService.findStorageKeyById(storageId);
		String pathAndName = StringUtils.concat(true, path, name);
		return StringUtils.concat(domain, PROXY_UPLOAD_LINK_PREFIX, storageKey, pathAndName);
	}

	/**
	 * 上传文件
	 *
	 * @param   pathAndName
	 *          文件上传路径
	 *
	 * @param   inputStream
	 *          文件流
	 *
	 */
	public abstract void uploadFile(String pathAndName, InputStream inputStream);


	/**
	 * 代理下载指定文件
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @return  文件响应.
	 */
	public abstract ResponseEntity<Resource> downloadToStream(String pathAndName) throws IOException;

}