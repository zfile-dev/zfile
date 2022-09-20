package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import im.zhaojun.zfile.core.constant.ZFileConstant;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.WebdavParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class WebdavServiceImpl extends AbstractProxyTransferService<WebdavParam> {

	private Sardine sardine;

	private String getRequestPath(String... strs) {
		return StringUtils.concat(param.getUrl(), StringUtils.encodeAllIgnoreSlashes(StringUtils.concat(strs)));
	}

	@SneakyThrows
	@Override
	public void init() {
		if (StrUtil.isAllNotEmpty(param.getUsername(), param.getPassword())) {
			sardine = SardineFactory.begin(param.getUsername(), param.getPassword());
		} else {
			sardine = SardineFactory.begin();
		}

		// 设置每次发请求都设置请求头，防止身份验证导致的无法上传成功.
		String host = new URL(param.getUrl()).getHost();
		sardine.enablePreemptiveAuthentication(host);

		testConnection();
		isInitialized = true;
	}

	@Override
	public List<FileItemResult> fileList(String folderPath) throws Exception {
		List<FileItemResult> resultList = new ArrayList<>();

		String requestUrl = getRequestPath(folderPath);
		String requestPath = URLUtil.getPath(requestUrl);

		List<DavResource> resources = sardine.list(requestUrl);
		for (DavResource davResource : resources) {
			if (Objects.equals(StringUtils.concat(requestPath, ZFileConstant.PATH_SEPARATOR),
								StringUtils.concat(davResource.getPath(), ZFileConstant.PATH_SEPARATOR))) {
				continue;
			}

			FileItemResult fileItemResult = davResourceToFileItem(davResource, folderPath);
			resultList.add(fileItemResult);
		}
		return resultList;
	}


	@Override
	public StorageTypeEnum getStorageTypeEnum() {
		return StorageTypeEnum.WEBDAV;
	}


	@Override
	public FileItemResult getFileItem(String pathAndName) {
		try {
			String requestUrl = getRequestPath(pathAndName);
			List<DavResource> resources = sardine.list(requestUrl);
			String folderPath = StringUtils.getParentPath(pathAndName);

			DavResource davResource = CollUtil.getLast(resources);
			return davResourceToFileItem(davResource, folderPath);
		} catch (Exception e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public boolean newFolder(String path, String name) {
		try {
			sardine.createDirectory(getRequestPath(path, name));
			return true;
		} catch (Exception e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public boolean deleteFile(String path, String name) {
		try {
			sardine.delete(getRequestPath(path, name));
			return true;
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public boolean deleteFolder(String path, String name) {
		return deleteFile(path, name);
	}

	@Override
	public boolean renameFolder(String path, String name, String newName) {
		return renameFile(path, name, newName);
	}

	@Override
	public boolean renameFile(String path, String name, String newName) {
		try {
			sardine.move(getRequestPath(path, name), getRequestPath(path, newName));
			return true;
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) {
		RequestHolder.writeFile(s -> {
			try {
				return sardine.get(getRequestPath(pathAndName));
			} catch (IOException e) {
				throw ExceptionUtil.wrapRuntime(e);
			}
		}, pathAndName);
		return null;
	}

	@Override
	public void uploadFile(String pathAndName, InputStream inputStream) {
		try {
			pathAndName = getRequestPath(pathAndName);
			sardine.put(pathAndName, inputStream);
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	private FileItemResult davResourceToFileItem(DavResource davResource, String folderPath) {
		FileItemResult fileItemResult = new FileItemResult();
		fileItemResult.setName(davResource.getName());
		fileItemResult.setTime(davResource.getModified());
		fileItemResult.setSize(davResource.getContentLength());
		fileItemResult.setType(davResource.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
		fileItemResult.setPath(folderPath);
		if (fileItemResult.getType() == FileTypeEnum.FILE) {
			fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(folderPath, fileItemResult.getName())));
		}
		return fileItemResult;
	}

}