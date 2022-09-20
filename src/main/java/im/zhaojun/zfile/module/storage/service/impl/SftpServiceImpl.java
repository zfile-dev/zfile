package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import im.zhaojun.zfile.core.exception.file.operator.DisableProxyDownloadException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.SftpParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.base.AbstractProxyTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class SftpServiceImpl extends AbstractProxyTransferService<SftpParam> {

	private Sftp sftp;

	@Override
	public void init() {
		sftp = new Sftp(param.getHost(), param.getPort(), param.getUsername(), param.getPassword(), StandardCharsets.UTF_8);
		testConnection();
		isInitialized = true;
	}

	@Override
	public List<FileItemResult> fileList(String folderPath) throws Exception {
		sftp.reconnectIfTimeout();
		List<FileItemResult> result = new ArrayList<>();

		String fullPath = StringUtils.concat(param.getBasePath(), folderPath);
		List<ChannelSftp.LsEntry> entryList = sftp.lsEntries(fullPath);
		for (ChannelSftp.LsEntry sftpEntry : entryList) {
			FileItemResult fileItemResult = sftpEntryToFileItem(sftpEntry, folderPath);
			result.add(fileItemResult);
		}
		return result;
	}


	@Override
	public StorageTypeEnum getStorageTypeEnum() {
		return StorageTypeEnum.SFTP;
	}


	@Override
	public FileItemResult getFileItem(String pathAndName) {
		sftp.reconnectIfTimeout();

		String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);
		List<ChannelSftp.LsEntry> entryList = sftp.lsEntries(fullPath);

		if (CollUtil.isEmpty(entryList)) {
			throw ExceptionUtil.wrapRuntime(new FileNotFoundException());
		}

		ChannelSftp.LsEntry sftpEntry = CollUtil.getFirst(entryList);
		String folderName = StringUtils.getParentPath(pathAndName);
		return sftpEntryToFileItem(sftpEntry, folderName);
	}


	@Override
	public boolean newFolder(String path, String name) {
		sftp.mkdir(StringUtils.concat(param.getBasePath(), path, name));
		return true;
	}


	@Override
	public synchronized boolean deleteFile(String path, String name) {
		return sftp.delFile(StringUtils.concat(param.getBasePath(), path, name));
	}


	@Override
	public synchronized boolean deleteFolder(String path, String name) {
		return sftp.delDir(StringUtils.concat(param.getBasePath(), path, name));
	}


	@Override
	public boolean renameFile(String path, String name, String newName) {
		sftp.reconnectIfTimeout();
		String srcPath = StringUtils.concat(param.getBasePath(), path, name);
		String distPath = StringUtils.concat(param.getBasePath(), path, newName);
		try {
			sftp.getClient().rename(srcPath, distPath);
			return true;
		} catch (SftpException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}


	@Override
	public boolean renameFolder(String path, String name, String newName) {
		return renameFile(path, name, newName);
	}


	@Override
	public synchronized ResponseEntity<Resource> downloadToStream(String pathAndName) {
		// 如果配置了域名，还访问代理下载 URL, 则抛出异常进行提示.
		if (StrUtil.isNotEmpty(param.getDomain())) {
			throw new DisableProxyDownloadException(CodeMsg.STORAGE_SOURCE_FILE_DISABLE_PROXY_DOWNLOAD, storageId);
		}

		HttpServletResponse response = RequestHolder.getResponse();
		try {
			pathAndName = StringUtils.concat(param.getBasePath(), pathAndName);
			String fileName = FileUtil.getName(pathAndName);
			
			OutputStream outputStream = response.getOutputStream();
			
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM.getType());
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + StringUtils.encodeAllIgnoreSlashes(fileName));
			
			sftp.download(pathAndName, outputStream);
			return null;
		} catch (IOException e) {
			throw ExceptionUtil.wrapRuntime(e);
		}
	}


	@Override
	public synchronized void uploadFile(String pathAndName, InputStream inputStream) {
		String fullPath = StringUtils.concat(param.getBasePath(), pathAndName);
		String fileName = FileUtil.getName(pathAndName);
		String folderName = FileUtil.getParent(fullPath, 1);
		sftp.upload(folderName, fileName, inputStream);
	}


	public FileItemResult sftpEntryToFileItem(ChannelSftp.LsEntry sftpEntry, String folderPath) {
		FileItemResult fileItemResult = new FileItemResult();
		fileItemResult.setName(sftpEntry.getFilename());
		fileItemResult.setTime(DateUtil.date(sftpEntry.getAttrs().getMTime() * 1000L));
		fileItemResult.setSize(sftpEntry.getAttrs().getSize());
		fileItemResult.setType(sftpEntry.getAttrs().isDir() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
		fileItemResult.setPath(folderPath);
		if (fileItemResult.getType() == FileTypeEnum.FILE) {
			fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(folderPath, fileItemResult.getName())));
		}
		return fileItemResult;
	}

}