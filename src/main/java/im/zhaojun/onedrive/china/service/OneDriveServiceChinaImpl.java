package im.zhaojun.onedrive.china.service;

import im.zhaojun.common.config.GlobalScheduleTask;
import im.zhaojun.common.exception.NotExistFileException;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@Slf4j
public class OneDriveServiceChinaImpl extends AbstractFileService implements FileService {

    @Resource
    private GlobalScheduleTask globalScheduleTask;

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private OneDriveChinaService oneDriveChinaService;

    @Override
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(getStorageTypeEnum());
            String accessToken = stringStorageConfigMap.get(StorageConfigConstant.ACCESS_TOKEN_KEY).getValue();
            String refreshToken = stringStorageConfigMap.get(StorageConfigConstant.REFRESH_TOKEN_KEY).getValue();

            if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
                log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
                isInitialized = false;
            } else {
                globalScheduleTask.refreshOneDriveToken(getStorageTypeEnum());
                isInitialized = testConnection();
            }
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常, 已跳过");
        }
    }

    @Override
    public List<FileItemDTO> fileList(String path) {
        return oneDriveChinaService.list(path);
    }

    @Override
    public String getDownloadUrl(String path) {
        return null;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.ONE_DRIVE_CHINA;
    }

    @Override
    public FileItemDTO getFileItem(String path) {
        FileItemDTO fileItemDTO ;

        try {
            fileItemDTO = oneDriveChinaService.getItem(path);
        } catch (Exception e) {
            throw new NotExistFileException();
        }
        return fileItemDTO;
    }
}
