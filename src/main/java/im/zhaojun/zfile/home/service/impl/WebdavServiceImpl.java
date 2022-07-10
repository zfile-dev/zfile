package im.zhaojun.zfile.home.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import im.zhaojun.zfile.admin.model.param.WebdavParam;
import im.zhaojun.zfile.common.constant.ZFileConstant;
import im.zhaojun.zfile.common.exception.FileUploadException;
import im.zhaojun.zfile.common.exception.file.operator.GetFileInfoException;
import im.zhaojun.zfile.common.util.RequestHolder;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import im.zhaojun.zfile.home.service.base.ProxyTransferService;
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
public class WebdavServiceImpl extends ProxyTransferService<WebdavParam> {

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
			throw new GetFileInfoException(storageId, pathAndName, e);
		}
	}

	@Override
	public boolean newFolder(String path, String name) {
		try {
			sardine.createDirectory(getRequestPath(path, name));
		} catch (Exception e) {
			log.error("webdav newFolder error, path: {}, name: {}", path, name, e);
			return false;
		}
		return true;
	}

	@Override
	public boolean deleteFile(String path, String name) {
		try {
			sardine.delete(getRequestPath(path, name));
			return true;
		} catch (IOException e) {
			log.error("webdav deleteFile error, path: {}, name: {}", path, name, e);
			return false;
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
		} catch (IOException e) {
			log.error("webdav renameFile error, path: {}, name: {}, newName: {}", path, name, newName, e);
			return false;
		}
		return true;
	}

	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) {
		RequestHolder.writeFile(s -> {
			try {
				return sardine.get(getRequestPath(pathAndName));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, pathAndName);
		return null;
	}

	public void uploadFile(String path, InputStream inputStream) {
		try {
			path = getRequestPath(path);
			sardine.put(path, inputStream);
		} catch (IOException e) {
			throw new FileUploadException(getStorageTypeEnum(), storageId, path, e);
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