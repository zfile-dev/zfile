package im.zhaojun.common.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.BooleanUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
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
    private FileCacheService fileCacheService;

    private Class<SystemConfigDTO> systemConfigDTOClass = SystemConfigDTO.class;

    public SystemConfigDTO getSystemConfig() {
        Object cache = configCache.get(SYSTEM_CONFIG_CACHE_KEY);
        if (configCache.get(SYSTEM_CONFIG_CACHE_KEY) != null) {
            return (SystemConfigDTO) cache;
        }

        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        List<SystemConfig> systemConfigList = systemConfigRepository.findAll();

        for (SystemConfig systemConfig : systemConfigList) {
            String key = systemConfig.getKey();

            try {
                Field field = systemConfigDTOClass.getDeclaredField(key);
                if (field != null) {
                    field.setAccessible(true);
                    String strVal = systemConfig.getValue();
                    Object convertVal = Convert.convert(field.getType(), strVal);
                    field.set(systemConfigDTO, convertVal);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                if (log.isDebugEnabled()) {
                    log.debug("通过反射, 将字段 {" + key + "}注入 SystemConfigDTO 时出现异常:", e);
                }
            }
        }

        configCache.put(SYSTEM_CONFIG_CACHE_KEY, systemConfigDTO);
        return systemConfigDTO;
    }


    public void updateSystemConfig(SystemConfigDTO systemConfigDTO) throws Exception {
        List<SystemConfig> systemConfigList = new ArrayList<>();

        Field[] fields = systemConfigDTOClass.getDeclaredFields();
        for (Field field : fields) {
            String key = field.getName();
            SystemConfig systemConfig = systemConfigRepository.findByKey(key);
            if (systemConfig != null) {
                field.setAccessible(true);
                Object val = field.get(systemConfigDTO);
                if (val != null) {
                    systemConfig.setValue(val.toString());
                    systemConfigList.add(systemConfig);
                }
            }
        }

        boolean oldEnableCache = getEnableCache();
        boolean curEnableCache = BooleanUtil.isTrue(systemConfigDTO.getEnableCache());

        configCache.remove(SYSTEM_CONFIG_CACHE_KEY);

        systemConfigRepository.saveAll(systemConfigList);

        if (!oldEnableCache && curEnableCache) {
            log.debug("检测到开启了缓存, 开启预热缓存");
            fileCacheService.enableCache();
        }

        if (oldEnableCache && !curEnableCache) {
            log.debug("检测到关闭了缓存, 正在清理缓存数据及关闭自动刷新");
            fileCacheService.disableCache();
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


    public void updateCacheEnableConfig(Boolean isEnable) {
        SystemConfig enableConfig = systemConfigRepository.findByKey(SystemConfigConstant.ENABLE_CACHE);
        enableConfig.setValue(isEnable.toString());
        systemConfigRepository.save(enableConfig);
        configCache.remove(SYSTEM_CONFIG_CACHE_KEY);
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
        return BooleanUtil.isTrue(systemConfigDTO.getEnableCache());
    }

    public boolean getSearchIgnoreCase() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return BooleanUtil.isTrue(systemConfigDTO.getSearchIgnoreCase());
    }


}