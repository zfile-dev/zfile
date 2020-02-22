package im.zhaojun.local.service;

import im.zhaojun.common.exception.NotExistFileException;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.SystemConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.constant.SystemConfigConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.repository.SystemConfigRepository;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Service
public class LocalServiceImpl extends AbstractFileService implements FileService {

    private static final Logger log = LoggerFactory.getLogger(LocalServiceImpl.class);

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private SystemConfigRepository systemConfigRepository;

    private String filePath;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(getStorageTypeEnum());
            filePath = stringStorageConfigMap.get(StorageConfigConstant.FILE_PATH_KEY).getValue();
            if (Objects.isNull(filePath)) {
                log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
                isInitialized = false;
            } else {
                isInitialized = testConnection();
            }
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常, 已跳过");
        }
    }

    @Override
    public List<FileItemDTO> fileList(String path) {
        List<FileItemDTO> fileItemList = new ArrayList<>();

        String fullPath = StringUtils.concatPath(filePath, path);

        File file = new File(fullPath);
        File[] files = file.listFiles();

        if (files == null) {
            return fileItemList;
        }
        for (File f : files) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            fileItemDTO.setType(f.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
            fileItemDTO.setTime(new Date(f.lastModified()));
            fileItemDTO.setSize(f.length());
            fileItemDTO.setName(f.getName());
            fileItemDTO.setPath(path);
            if (f.isFile()) {
                fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, f.getName())));
            }
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) {
        SystemConfig usernameConfig = systemConfigRepository.findByKey(SystemConfigConstant.DOMAIN);
        return StringUtils.removeDuplicateSeparator(usernameConfig.getValue() + "/file/" + path);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.LOCAL;
    }

    @Override
    public FileItemDTO getFileItem(String path) {
        String fullPath = StringUtils.concatPath(filePath, path);

        File file = new File(fullPath);

        if (!file.exists()) {
            throw new NotExistFileException();
        }

        FileItemDTO fileItemDTO = new FileItemDTO();
        fileItemDTO.setType(file.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
        fileItemDTO.setTime(new Date(file.lastModified()));
        fileItemDTO.setSize(file.length());
        fileItemDTO.setName(file.getName());
        fileItemDTO.setPath(filePath);
        if (file.isFile()) {
            fileItemDTO.setUrl(getDownloadUrl(path));
        }

        return fileItemDTO;
    }
}