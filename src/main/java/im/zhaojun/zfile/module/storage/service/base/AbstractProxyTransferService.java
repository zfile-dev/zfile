package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.net.url.UrlBuilder;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.ProxyDownloadUrlUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.model.param.ProxyTransferParam;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;

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

	@Resource
	private SystemConfigService systemConfigService;


	@Resource
	private StorageSourceService storageSourceService;


	/**
	 * 获取默认代理下载 URL.
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @return  默认的代理下载 URL
	 */
	public String getProxyDownloadUrl(String pathAndName) {
		return getProxyDownloadUrl(pathAndName, false);
	}


	/**
	 * 获取默认代理下载 URL.
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @param 	useParamDomain
	 * 			是否使用存储源参数中的域名替换系统配置中的域名作为下载地址
	 *
	 * @return  默认的代理下载 URL
	 */
	public String getProxyDownloadUrl(String pathAndName, boolean useParamDomain) {
		String path = pathAndName;

		UrlBuilder urlBuilder = UrlBuilder.of();
		String filename = FileUtils.getName(pathAndName);
		if (filename.startsWith(".")) {
			urlBuilder.addQuery("filename", filename);
			path = FileUtils.getParentPath(pathAndName);
		}

		if (param.isProxyPrivate()) {
			urlBuilder.addQuery("signature", ProxyDownloadUrlUtils.generatorSignature(storageId, pathAndName, param.getProxyTokenTime()));
		}

		String url;

		// 如果未填写下载域名，则默认使用带来下载地址.
		if (!useParamDomain || StringUtils.isEmpty(param.getDomain())) {
			String domain = systemConfigService.getAxiosFromDomainOrSetting();
			String storageKey = storageSourceService.findStorageKeyById(storageId);
			url = StringUtils.concat(domain, PROXY_DOWNLOAD_LINK_PREFIX, storageKey, StringUtils.encodeAllIgnoreSlashes(path));
		} else {
			url = StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(path));
		}

		if (StringUtils.isNotEmpty(urlBuilder.getQueryStr())) {
			url = url + "?" + urlBuilder.getQueryStr();
		}
		return url;
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
	 * @return  默认的代理下上传 URL
	 */
	public String getProxyUploadUrl(String path, String name) {
		String domain = systemConfigService.getAxiosFromDomainOrSetting();
		String storageKey = storageSourceService.findStorageKeyById(storageId);

		UrlBuilder urlBuilder = UrlBuilder.of();

		String fullPath = StringUtils.concat(path, name);

		// 以 . 开头的文件名, 代表是隐藏文件, 需要特殊处理为参数形式，不然会被安全拦截.
		if (name.startsWith(".")) {
			urlBuilder.addQuery("filename", name);
			fullPath = path;
		}

		String uploadUrl = StringUtils.concat(domain, PROXY_UPLOAD_LINK_PREFIX, storageKey, StringUtils.encodeAllIgnoreSlashes(fullPath));

		if (StringUtils.isNotEmpty(urlBuilder.getQueryStr())) {
			uploadUrl = uploadUrl + "?" + urlBuilder.getQueryStr();
		}

		return uploadUrl;
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
	 * @param 	size
	 * 			文件大小
	 */
	public abstract void uploadFile(String pathAndName, InputStream inputStream, Long size) throws Exception;


	/**
	 * 代理下载指定文件
	 *
	 * @param   pathAndName
	 *          文件路径及文件名称
	 *
	 * @return  文件响应.
	 */
	public abstract ResponseEntity<org.springframework.core.io.Resource> downloadToStream(String pathAndName) throws Exception;

    protected SystemConfigService getSystemConfigService() {
        return systemConfigService;
    }

}