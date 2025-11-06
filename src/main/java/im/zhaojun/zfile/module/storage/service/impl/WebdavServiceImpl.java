package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.URLUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.io.ContentLengthInputStream;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.WebdavParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import im.zhaojun.zfile.module.storage.support.webdav.CustomSardine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
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

	public static final Duration connectTimeoutSecond = Duration.ofSeconds(10);

	private Sardine sardine;

	private String getRequestPath(String... strs) {
		return getRequestPath(true, strs);
	}

	private String getRequestPath(boolean containUserBasePath, String... strs) {
		return StringUtils.concat(param.getUrl(),
				StringUtils.encodeAllIgnoreSlashes(param.getBasePath()),
				containUserBasePath ? StringUtils.encodeAllIgnoreSlashes(getCurrentUserBasePath()) : "",
				StringUtils.encodeAllIgnoreSlashes(StringUtils.concat(strs)));
	}

	@SneakyThrows
	@Override
	public void init() {
		sardine = new CustomSardine(param.getUsername(), param.getPassword(), connectTimeoutSecond, null);
		String host = URI.create(param.getUrl()).getHost();
		sardine.enablePreemptiveAuthentication(host);
	}

	@Override
	public List<FileItemResult> fileList(String folderPath) throws Exception {
		List<FileItemResult> resultList = new ArrayList<>();

		String requestUrl = getRequestPath(folderPath);
		String requestPath = URLUtil.getPath(requestUrl);

		List<DavResource> resources = sardine.list(requestUrl);
		for (DavResource davResource : resources) {
			if (Objects.equals(StringUtils.concat(requestPath, StringUtils.SLASH),
								StringUtils.concat(davResource.getPath(), StringUtils.SLASH))) {
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
		return getFileItem(pathAndName, true);
	}

    public FileItemResult getFileItem(String pathAndName, boolean containUserBasePath) {
        try {
            String requestUrl = getRequestPath(containUserBasePath, pathAndName);

            List<DavResource> resources = sardine.list(requestUrl, 0);

            DavResource davResource = resources.isEmpty() ? null : resources.get(0);

            if (davResource == null) {
                return null;
            }

            String folderPath = FileUtils.getParentPath(pathAndName);
            return davResourceToFileItem(davResource, folderPath);
        } catch (Exception e) {
            if (e instanceof SardineException && ((SardineException) e).getStatusCode() == 404) {
                return null;
            }
            throw ExceptionUtil.wrapRuntime(e);
        }
    }

	@Override
	public boolean newFolder(String path, String name) {
		try {
			String requestPath = getRequestPath(path, name);
			sardine.createDirectory(requestPath + "/");
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
		return moveFolder(path, name, path, newName);
	}

	@Override
	public boolean renameFile(String path, String name, String newName) {
		return moveFolder(path, name, path, newName);
	}


	@Override
	public String getDownloadUrl(String pathAndName) {
		if (param.isRedirectMode()) {
			return getRequestPath(false, pathAndName);
		}
		if (StringUtils.isNotBlank(param.getDomain())) {
			return StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(StringUtils.concat(param.getBasePath(), pathAndName)));
		}
		return super.getProxyDownloadUrl(pathAndName);
	}


	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) throws IOException {
		ContentLengthInputStream inputStream = (ContentLengthInputStream) sardine.get(getRequestPath(false, pathAndName));
		String fileName = FileUtils.getName(pathAndName);
		RequestHolder.writeFile(inputStream, fileName, inputStream.getLength(), false, param.isProxyLinkForceDownload());
		return null;
	}

	@Override
	public String getUploadUrl(String path, String name, Long size) {
		return super.getProxyUploadUrl(path, name);
	}

	@Override
	public void uploadFile(String pathAndName, InputStream inputStream, Long size) {
		try {
			pathAndName = getRequestPath(pathAndName);
			sardine.put(pathAndName, inputStream, null, true, size);
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
			fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName())));
		}
		return fileItemResult;
	}

	@Override
	public boolean copyFile(String path, String name, String targetPath, String targetName) {
		return copyFolder(path, name, targetPath, targetName);
	}

	@Override
	public boolean copyFolder(String path, String name, String targetPath, String targetName) {
		try {
			sardine.copy(getRequestPath(path, name), getRequestPath(targetPath, targetName));
			return true;
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public boolean moveFile(String path, String name, String targetPath, String targetName) {
		return moveFolder(path, name, targetPath, targetName);
	}

	@Override
	public boolean moveFolder(String path, String name, String targetPath, String targetName) {
		try {
			sardine.move(getRequestPath(path, name) + "/", getRequestPath(targetPath, targetName) + "/");
			return true;
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}

	@Override
	public StorageSourceMetadata getStorageSourceMetadata() {
		StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
		storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
		return storageSourceMetadata;
	}


	@Override
	public void destroy() {
		if (sardine != null) {
			try {
				sardine.shutdown();
			} catch (IOException e) {
				log.error("WebDAV 服务关闭失败", e);
			}
		}
	}
}