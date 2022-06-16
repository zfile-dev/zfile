package im.zhaojun.zfile.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import im.zhaojun.zfile.exception.NotExistFileException;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebdavServiceImpl extends AbstractBaseFileService {

	private Sardine sardine;

	private String url;

	@Resource
	private StorageConfigService storageConfigService;

	@Override
	public void init(Integer driveId) {
		this.driveId = driveId;
		Map<String, StorageConfig> stringStorageConfigMap =
				storageConfigService.selectStorageConfigMapByDriveId(driveId);
		this.mergeStrategyConfig(stringStorageConfigMap);
		String username = stringStorageConfigMap.get(StorageConfigConstant.WEBDAV_USERNAME).getValue();
		String password = stringStorageConfigMap.get(StorageConfigConstant.WEBDAV_PASSWORD).getValue();
		url = stringStorageConfigMap.get(StorageConfigConstant.WEBDAV_URL).getValue();

		if (Objects.isNull(url)) {
			log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
			isInitialized = false;
		} else {
			// 如果用户名和密码为空，则使用默认用户名和密码
			if (StrUtil.isNotEmpty(username) && StrUtil.isNotEmpty(password)) {
				sardine = SardineFactory.begin(username, password);
			} else {
				sardine = SardineFactory.begin();
			}
			testConnection();
			isInitialized = true;
		}
	}

	@Override
	public List<FileItemDTO> fileList(String path) throws Exception {
		List<FileItemDTO> resultList = new ArrayList<>();

		String requestPath = StringUtils.removeDuplicateSeparator(url + "/" + path);

		List<DavResource> resources = sardine.list(requestPath);

		Integer index = 0;

		for (DavResource res : resources) {
			// 如果不是根目录, 则跳过第一个, 因为第一个是当前目录
			if (!StrUtil.equals(path, "/") && index++ == 0) {
				continue;
			}

			FileItemDTO fileItemResult = new FileItemDTO();
			fileItemResult.setName(res.getName());
			fileItemResult.setTime(res.getModified());
			fileItemResult.setSize(res.getContentLength());
			fileItemResult.setType(res.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
			fileItemResult.setPath(path);
			fileItemResult.setUrl(getDownloadUrl(path + res.getName()));
			resultList.add(fileItemResult);
		}
		return resultList;
	}

	@Override
	public String getDownloadUrl(String path) {
		return StringUtils.concatPath(url, path);
	}

	@Override
	public StorageTypeEnum getStorageTypeEnum() {
		return StorageTypeEnum.WebDAV;
	}

	@Override
	public List<StorageConfig> storageStrategyConfigList() {
		return new ArrayList<StorageConfig>() {{
			add(new StorageConfig("webdavUsername", "用户名"));
			add(new StorageConfig("webdavPassword", "密码"));
			add(new StorageConfig("webdavUrl", "WebDav 链接"));
		}};
	}

	@Override
	public FileItemDTO getFileItem(String path) {
		List<FileItemDTO> list;
		try {
			int end = path.lastIndexOf("/");
			list = fileList(path.substring(0, end + 1));
		} catch (Exception e) {
			throw new NotExistFileException();
		}

		for (FileItemDTO fileItemDTO : list) {
			String fullPath = StringUtils.concatUrl(fileItemDTO.getPath(), fileItemDTO.getName());
			if (Objects.equals(fullPath, path)) {
				return fileItemDTO;
			}
		}

		throw new NotExistFileException();
	}
}