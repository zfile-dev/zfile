package im.zhaojun.common.service;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.SystemConfig;
import im.zhaojun.common.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SystemConfigService {

    @Resource
    private SystemConfigRepository systemConfigRepository;

    public SystemConfig getSystemConfig() {
        return systemConfigRepository.findFirstBy();
    }

    public FileService getCurrentFileService() {
        SystemConfig systemConfig = getSystemConfig();
        StorageTypeEnum storageStrategy = systemConfig.getStorageStrategy();
        return StorageTypeFactory.getTrafficMode(storageStrategy);
    }

}