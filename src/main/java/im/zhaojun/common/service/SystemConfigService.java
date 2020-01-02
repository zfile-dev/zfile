package im.zhaojun.common.service;

import cn.hutool.crypto.SecureUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.model.SystemConfig;
import im.zhaojun.common.model.constant.SystemConfigConstant;
import im.zhaojun.common.model.dto.SystemConfigDTO;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.repository.SystemConfigRepository;
import im.zhaojun.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaojun
 */
@Slf4j
@Service
public class SystemConfigService {

    public static final String SYSTEM_CONFIG_CACHE_PREFIX = "zfile-config-cache:";

    public static final String SYSTEM_CONFIG_CACHE_KEY = "1";

    @CreateCache(name = SYSTEM_CONFIG_CACHE_PREFIX, cacheType = CacheType.LOCAL)
    private Cache<String, Object> configCache;

    @Resource
    private SystemConfigRepository systemConfigRepository;

    @Resource
    private FileAsyncCacheService fileAsyncCacheService;

    public SystemConfigDTO getSystemConfig() {
        Object cache = configCache.get(SYSTEM_CONFIG_CACHE_KEY);
        if (configCache.get(SYSTEM_CONFIG_CACHE_KEY) != null) {
            return (SystemConfigDTO) cache;
        }

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
                case SystemConfigConstant.DOMAIN:
                    systemConfigDTO.setDomain(systemConfig.getValue());
                    break;
                case SystemConfigConstant.ENABLE_CACHE:
                    systemConfigDTO.setEnableCache("true".equals(systemConfig.getValue()));
                    break;
                default:break;
            }
        }

        configCache.put(SYSTEM_CONFIG_CACHE_KEY, systemConfigDTO);
        return systemConfigDTO;
    }

    public void updateSystemConfig(SystemConfigDTO systemConfigDTO) throws Exception {

        List<SystemConfig> systemConfigList = new ArrayList<>();

        SystemConfig systemConfig = systemConfigRepository.findByKey(SystemConfigConstant.SITE_NAME);
        systemConfig.setValue(systemConfigDTO.getSiteName());
        systemConfigList.add(systemConfig);

        SystemConfig domainConfig = systemConfigRepository.findByKey(SystemConfigConstant.DOMAIN);
        domainConfig.setValue(systemConfigDTO.getDomain());
        systemConfigList.add(domainConfig);

        SystemConfig infoEnableSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.INFO_ENABLE);
        infoEnableSystemConfig.setValue(systemConfigDTO.getInfoEnable() ? "true" : "false");
        systemConfigList.add(infoEnableSystemConfig);

        SystemConfig searchEnableSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.SEARCH_ENABLE);
        searchEnableSystemConfig.setValue(systemConfigDTO.getSearchEnable() ? "true" : "false");
        systemConfigList.add(searchEnableSystemConfig);

        SystemConfig searchIgnoreCaseSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.SEARCH_IGNORE_CASE);
        searchIgnoreCaseSystemConfig.setValue(systemConfigDTO.getSearchIgnoreCase() ? "true" : "false");
        systemConfigList.add(searchIgnoreCaseSystemConfig);

        boolean oldEnableCache = getEnableCache();
        Boolean curEnableCache = systemConfigDTO.getEnableCache();

        SystemConfig enableCacheSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.ENABLE_CACHE);
        enableCacheSystemConfig.setValue(systemConfigDTO.getEnableCache() ? "true" : "false");
        systemConfigList.add(enableCacheSystemConfig);

        SystemConfig storageStrategySystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.STORAGE_STRATEGY);
        storageStrategySystemConfig.setValue(systemConfigDTO.getStorageStrategy().getKey());
        systemConfigList.add(storageStrategySystemConfig);

        if (StringUtils.isNotNullOrEmpty(systemConfigDTO.getUsername())) {
            SystemConfig usernameSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.USERNAME);
            usernameSystemConfig.setValue(systemConfigDTO.getUsername());
            systemConfigList.add(usernameSystemConfig);
        }

        if (StringUtils.isNotNullOrEmpty(systemConfigDTO.getPassword())) {
            SystemConfig passwordSystemConfig = systemConfigRepository.findByKey(SystemConfigConstant.PASSWORD);
            passwordSystemConfig.setValue(systemConfigDTO.getPassword());
            systemConfigList.add(passwordSystemConfig);
        }

        configCache.remove(SYSTEM_CONFIG_CACHE_KEY);

        systemConfigRepository.saveAll(systemConfigList);

        if (!oldEnableCache && curEnableCache) {
            log.debug("检测到开启了缓存, 开启预热缓存");
            fileAsyncCacheService.cacheGlobalFile();
        }
    }

    public void updateUsernameAndPwd(String username, String password) {
        SystemConfig usernameConfig = systemConfigRepository.findByKey(SystemConfigConstant.USERNAME);
        usernameConfig.setValue(username);
        systemConfigRepository.save(usernameConfig);

        String encryptionPassword = SecureUtil.md5(password);
        SystemConfig systemConfig = systemConfigRepository.findByKey(SystemConfigConstant.PASSWORD);
        systemConfig.setValue(encryptionPassword);

        configCache.remove(SYSTEM_CONFIG_CACHE_KEY);

        systemConfigRepository.save(systemConfig);
    }

    public AbstractFileService getCurrentFileService() {
        StorageTypeEnum storageStrategy = getCurrentStorageStrategy();
        return StorageTypeFactory.getStorageTypeService(storageStrategy);
    }

    public StorageTypeEnum getCurrentStorageStrategy() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getStorageStrategy();
    }

    public boolean getEnableCache() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getEnableCache();
    }

}