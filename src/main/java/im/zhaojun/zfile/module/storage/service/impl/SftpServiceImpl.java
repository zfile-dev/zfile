package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import im.zhaojun.zfile.core.util.*;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.SftpParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import im.zhaojun.zfile.module.storage.support.ftp.FtpClientFactory;
import im.zhaojun.zfile.module.storage.support.sftp.SFtpClientFactory;
import im.zhaojun.zfile.module.storage.support.sftp.SFtpClientPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class SftpServiceImpl extends AbstractProxyTransferService<SftpParam> {

	private SFtpClientPool sftpClientPool;

	@Override
	public void init() {
		Charset charset = Charset.forName(param.getEncoding());
		SFtpClientFactory factory = new SFtpClientFactory(param.getHost(), param.getPort(), param.getUsername(), param.getPassword(), param.getPrivateKey(), param.getPassphrase(), charset);
		GenericObjectPoolConfig<FtpClientFactory> config = new GenericObjectPoolConfig<>();
		config.setTestOnBorrow(true);
		config.setMaxTotal(param.getMaxConnections());
		// 2 分钟没有使用则进行回收
		config.setMinEvictableIdleDuration(Duration.ofMinutes(2));
		config.setMaxWait(Duration.ofSeconds(15));
		sftpClientPool = new SFtpClientPool(factory, config);
	}

	public Sftp getClientFromPool() {
		try {
			return sftpClientPool.borrowObject();
		} catch (NoSuchElementException e) {
			throw new BizException(ErrorCode.BIZ_SFTP_CLIENT_POOL_FULL);
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Override
	public List<FileItemResult> fileList(String folderPath) throws Exception {
		List<FileItemResult> result = new ArrayList<>();

		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), folderPath);
			List<ChannelSftp.LsEntry> entryList = sftp.lsEntries(fullPath);
			for (ChannelSftp.LsEntry sftpEntry : entryList) {
				FileItemResult fileItemResult = sftpEntryToFileItem(sftpEntry, folderPath);
				result.add(fileItemResult);
			}
			return result;
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}


	@Override
	public StorageTypeEnum getStorageTypeEnum() {
		return StorageTypeEnum.SFTP;
	}


	public FileItemResult getFileItem(String pathAndName, boolean containUserBasePath) {
		String fullPath = StringUtils.concat(param.getBasePath(), containUserBasePath ? getCurrentUserBasePath() : "", pathAndName);

		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			List<ChannelSftp.LsEntry> entryList = sftp.lsEntries(fullPath);

			if (CollectionUtils.isEmpty(entryList)) {
				return null;
			}

			ChannelSftp.LsEntry sftpEntry = CollectionUtils.getFirst(entryList);
			if (sftpEntry == null) {
				return null;
			}
			String folderName = FileUtils.getParentPath(pathAndName);
			return sftpEntryToFileItem(sftpEntry, folderName);
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}

	@Override
	public FileItemResult getFileItem(String pathAndName) {
		return getFileItem(pathAndName, true);
	}

	@Override
	public boolean newFolder(String path, String name) {
		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			sftp.mkdir(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name));
			return true;
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}


	@Override
	public boolean deleteFile(String path, String name) {
		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			return sftp.delFile(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name));
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}


	@Override
	public boolean deleteFolder(String path, String name) {
		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			return sftp.delDir(StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name));
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}

	@Override
	public boolean copyFile(String path, String name, String targetPath, String targetName) {
		throw new BizException(ErrorCode.BIZ_UNSUPPORTED_OPERATION);
	}

	@Override
	public boolean copyFolder(String path, String name, String targetPath, String targetName) {
		throw new BizException(ErrorCode.BIZ_UNSUPPORTED_OPERATION);
	}

	@Override
	public boolean moveFile(String path, String name, String targetPath, String targetName) {
		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			String srcPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
			String distPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), targetPath, targetName);
			sftp.getClient().rename(srcPath, distPath);
			return true;
		} catch (SftpException e) {
			throw new SystemException(e);
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}

	@Override
	public boolean moveFolder(String path, String name, String targetPath, String targetName) {
		return moveFile(path, name, targetPath, targetName);
	}


	@Override
	public boolean renameFile(String path, String name, String newName) {
		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			String srcPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, name);
			String distPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), path, newName);
			sftp.getClient().rename(srcPath, distPath);
			return true;
		} catch (SftpException e) {
			throw new SystemException(e);
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}


	@Override
	public boolean renameFolder(String path, String name, String newName) {
		return renameFile(path, name, newName);
	}


	@Override
	public ResponseEntity<Resource> downloadToStream(String pathAndName) throws Exception {
		// 如果配置了域名，还访问代理下载 URL, 则抛出异常进行提示.
		if (StringUtils.isNotEmpty(param.getDomain())) {
			throw new BizException(ErrorCode.BIZ_UNSUPPORTED_PROXY_DOWNLOAD);
		}

		FileItemResult fileItem = getFileItem(pathAndName, false);
		if (fileItem == null) {
			throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
		}

		long fileSize = fileItem.getSize();
		pathAndName = StringUtils.concat(param.getBasePath(), pathAndName);
		String fileName = FileUtils.getName(pathAndName);

		// 根据请求头中的 Range 参数, 获取要跳过的字节数.
		long skip = 0;
		HttpRange requestRange = RequestUtils.getRequestRange(RequestHolder.getRequest());
		if (requestRange != null) {
			skip = (int) requestRange.getRangeStart(fileSize);
		}

		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			InputStream inputStream = sftp.getClient().get(pathAndName, null, skip);
			RequestHolder.writeFile(inputStream, fileName, fileSize, true, param.isProxyLinkForceDownload());
			return null;
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}


	@Override
	public String getUploadUrl(String path, String name, Long size) {
		return super.getProxyUploadUrl(path, name);
	}


	@Override
	public String getDownloadUrl(String pathAndName) {
		if (StringUtils.isNotBlank(param.getDomain())) {
			return StringUtils.concat(param.getDomain(), StringUtils.encodeAllIgnoreSlashes(pathAndName));
		}
		return super.getProxyDownloadUrl(pathAndName);
	}

	@Override
	public void uploadFile(String pathAndName, InputStream inputStream, Long size) {
		String fullPath = StringUtils.concat(param.getBasePath(), getCurrentUserBasePath(), pathAndName);
		String fileName = FileUtils.getName(pathAndName);
		String folderName = FileUtils.getParentPath(fullPath);
		Sftp sftp = null;
		try {
			sftp = getClientFromPool();
			sftp.upload(folderName, fileName, inputStream);
		} finally {
			if (sftp != null) {
				sftpClientPool.returnObject(sftp);
			}
		}
	}


	public FileItemResult sftpEntryToFileItem(ChannelSftp.LsEntry sftpEntry, String folderPath) {
		FileItemResult fileItemResult = new FileItemResult();
		fileItemResult.setName(sftpEntry.getFilename());
		fileItemResult.setTime(DateUtil.date(sftpEntry.getAttrs().getMTime() * 1000L));
		fileItemResult.setSize(sftpEntry.getAttrs().isDir() ? null : sftpEntry.getAttrs().getSize());
		fileItemResult.setType(sftpEntry.getAttrs().isDir() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
		fileItemResult.setPath(folderPath);
		if (fileItemResult.getType() == FileTypeEnum.FILE) {
			fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), folderPath, fileItemResult.getName())));
		}
		return fileItemResult;
	}

	@Override
	public StorageSourceMetadata getStorageSourceMetadata() {
		StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
		storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
		return storageSourceMetadata;
	}

	@Override
	public void destroy() {
		if (sftpClientPool != null) {
			sftpClientPool.close();
		}
	}
}