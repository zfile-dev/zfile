package im.zhaojun.common.service;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.model.SystemConfig;
import im.zhaojun.common.model.constant.SystemConfigConstant;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.repository.SystemConfigRepository;
import im.zhaojun.common.util.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemConfigService {

    @Resource
    private SystemConfigRepository systemConfigRepository;

    public SystemConfigDTO getSystemConfig() {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        List<SystemConfig> systemConfigList = systemConfigRepository.findAll();

        for (SystemConfig systemConfig : systemConfigList) {
            switch (systemConfig.getKey()) {
                case SystemConfigConstant.SITE_NAME:
                    systemConfigDTO.setSiteName(systemConfig.getValue());
                    break;
                case SystemConfigConstant.INFO_ENABLE:
                    systemConfigDTO.setInfoEnable("true".equals(systemConfig.getValue()));
                    break;
                case SystemConfigConstant.SEARCH_ENABLE:
                    systemConfigDTO.setSearchEnable("true".equals(systemConfig.getValue()));
                    break;
                case SystemConfigConstant.SEARCH_IGNORE_CASE:
                    systemConfigDTO.setSearchIgnoreCase("true".equals(systemConfig.getValue()));
                    break;
                case SystemConfigConstant.STORAGE_STRATEGY:
                    String value = systemConfig.getValue();
                    systemConfigDTO.setStorageStrategy(StorageTypeEnum.getEnum(value));
                    break;
                case SystemConfigConstant.USERNAME:
                    systemConfigDTO.setUsername(systemConfig.getValue());
                    break;
                case SystemConfigConstant.PASSWORD:
                    systemConfigDTO.setPassword(systemConfig.getValue());
                    break;
                default:break;
            }
        }

        return systemConfigDTO;
    }

    public void updateSystemConfig(SystemConfigDTO systemConfigDTO) {
        List<SystemConfig> systemConfigList = new ArrayList<>();

        SystemConfig systemConfig = systemConfigRepository.findByKey(SystemConfigConstant.SITE_NAME);
        systemConfig.setValue(systemConfigDTO.getSiteName());
        systemConfigList.add(systemConfig);

        SystemConfig infoEnableSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.INFO_ENABLE);
        infoEnableSystemConfig.setValue(systemConfigDTO.getInfoEnable() ? "true" : "false");
        systemConfigList.add(infoEnableSystemConfig);

        SystemConfig searchEnableSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.SEARCH_ENABLE);
        searchEnableSystemConfig.setValue(systemConfigDTO.getSearchEnable() ? "true" : "false");
        systemConfigList.add(searchEnableSystemConfig);

        SystemConfig searchIgnoreCaseSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.SEARCH_IGNORE_CASE);
        searchIgnoreCaseSystemConfig.setValue(systemConfigDTO.getSearchIgnoreCase() ? "true" : "false");
        systemConfigList.add(searchIgnoreCaseSystemConfig);

        SystemConfig storageStrategySystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.STORAGE_STRATEGY);
        storageStrategySystemConfig.setValue(systemConfigDTO.getStorageStrategy().getKey());
        systemConfigList.add(storageStrategySystemConfig);

        if (!StringUtils.isNullOrEmpty(systemConfigDTO.getUsername())) {
            SystemConfig usernameSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.USERNAME);
            usernameSystemConfig.setValue(systemConfigDTO.getUsername());
            systemConfigList.add(usernameSystemConfig);
        }

        if (!StringUtils.isNullOrEmpty(systemConfigDTO.getPassword())) {
            SystemConfig passwordSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.PASSWORD);
            passwordSystemConfig.setValue(systemConfigDTO.getPassword());
            systemConfigList.add(passwordSystemConfig);
        }

        systemConfigRepository.saveAll(systemConfigList);
    }

    public void updateUsernameAndPwd(String username, String password) {
        SystemConfig usernameConfig = systemConfigRepository.findByKey(SystemConfigConstant.USERNAME);
        usernameConfig.setValue(username);
        systemConfigRepository.save(usernameConfig);

        password = new BCryptPasswordEncoder().encode(password);
        SystemConfig systemConfig = systemConfigRepository.findByKey(SystemConfigConstant.PASSWORD);
        systemConfig.setValue(password);

        systemConfigRepository.save(systemConfig);
    }

    public FileService getCurrentFileService() {
        StorageTypeEnum storageStrategy = getCurrentStorageStrategy();
        return StorageTypeFactory.getStorageTypeService(storageStrategy);
    }

    public StorageTypeEnum getCurrentStorageStrategy() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getStorageStrategy();
    }

}