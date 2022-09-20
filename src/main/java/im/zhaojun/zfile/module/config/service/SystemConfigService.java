package im.zhaojun.zfile.module.config.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import im.zhaojun.zfile.module.config.constant.SystemConfigConstant;
import im.zhaojun.zfile.module.config.mapper.SystemConfigMapper;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.login.model.enums.LoginVerifyModeEnum;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.exception.ServiceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.core.util.EnumConvertUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static im.zhaojun.zfile.module.config.service.SystemConfigService.CACHE_NAME;

/**
 * 系统设置 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
@CacheConfig(cacheNames = CACHE_NAME)
public class SystemConfigService {
    
    public static final String CACHE_NAME = "systemConfig";
    
    private static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "123456";

    private static final LoginVerifyModeEnum DEFAULT_LOGIN_VERIFY_MODE = LoginVerifyModeEnum.IMG_VERIFY_MODE;

    @Resource
    private SystemConfigMapper systemConfigMapper;
    
    @Resource
    private SystemConfigService systemConfigService;
    
    @Resource
    private ZFileProperties zFileProperties;
    
    @Resource
    private CacheManager cacheManager;
    
    private final Class<SystemConfigDTO> systemConfigClazz = SystemConfigDTO.class;

    
    /**
     * 获取系统设置, 如果缓存中有, 则去缓存取, 没有则查询数据库并写入到缓存中.
     *
     * @return  系统设置
     */
    @Cacheable(key = "'dto'")
    public SystemConfigDTO getSystemConfig() {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        List<SystemConfig> systemConfigList = systemConfigMapper.findAll();

        for (SystemConfig systemConfig : systemConfigList) {
            String key = systemConfig.getName();

            try {
                Field field = systemConfigClazz.getDeclaredField(key);
                field.setAccessible(true);
                String strVal = systemConfig.getValue();
                Class<?> fieldType = field.getType();

                Object convertVal;
                if (EnumUtil.isEnum(fieldType)) {
                    convertVal = EnumConvertUtils.convertStrToEnum(fieldType, strVal);
                } else {
                    convertVal = Convert.convert(fieldType, strVal);
                }
                field.set(systemConfigDTO, convertVal);
            } catch (NoSuchFieldException | IllegalAccessException | ConvertException e) {
                log.error("通过反射, 将字段 {} 注入 SystemConfigDTO 时出现异常:", key, e);
            }
        }

        return systemConfigDTO;
    }


    /**
     * 更新系统设置, 并清空缓存中的内容.
     *
     * @param   systemConfigDTO
     *          系统设置 dto
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public void updateSystemConfig(SystemConfigDTO systemConfigDTO) {
        List<SystemConfig> systemConfigList = new ArrayList<>();

        Field[] fields = systemConfigClazz.getDeclaredFields();
        for (Field field : fields) {
            String key = field.getName();
            SystemConfig systemConfig = systemConfigMapper.findByName(key);
            if (systemConfig != null) {
                field.setAccessible(true);
                Object val = null;

                try {
                    val = field.get(systemConfigDTO);
                } catch (IllegalAccessException e) {
                    log.error("通过反射, 从 SystemConfigDTO 获取字段 {}  时出现异常:", key, e);
                }

                if (val != null) {
                    // 如果是枚举类型, 则取 value 值.
                    if (EnumUtil.isEnum(val)) {
                        val = EnumConvertUtils.convertEnumToStr(val);
                    }
                    systemConfig.setValue(Convert.toStr(val));
                    systemConfigList.add(systemConfig);
                }
            }
        }

        systemConfigList.forEach(systemConfig -> systemConfigMapper.updateById(systemConfig));
    }


    /**
     * 重置管理员登录信息, 重置登录账号为 admin, 密码为 123456, 登录校验方式为 图形验证码.
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public void resetAdminLoginInfo() {
        if (!zFileProperties.isDebug()) {
            log.warn("当前为非调试模式, 无法重置管理员登录信息");
            throw new ServiceException(CodeMsg.BAD_REQUEST);
        }
        
        SystemConfig usernameConfig = systemConfigMapper.findByName(SystemConfigConstant.USERNAME);
        usernameConfig.setValue(DEFAULT_USERNAME);
        systemConfigMapper.updateById(usernameConfig);

        String encryptionPassword = SecureUtil.md5(DEFAULT_PASSWORD);
        SystemConfig passwordConfig = systemConfigMapper.findByName(SystemConfigConstant.PASSWORD);
        passwordConfig.setValue(encryptionPassword);
        systemConfigMapper.updateById(passwordConfig);

        SystemConfig loginVerifyModeConfig = systemConfigMapper.findByName(SystemConfigConstant.LOGIN_VERIFY_MODE);
        loginVerifyModeConfig.setValue(DEFAULT_LOGIN_VERIFY_MODE.getValue());
        systemConfigMapper.updateById(loginVerifyModeConfig);
    
        log.info("重置管理员登录信息成功, 账号: {}, 密码: {}, 登录校验方式: {}", DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_LOGIN_VERIFY_MODE);
    }

    
    /**
     * 获取 RSA Hex 格式密钥
     *
     * @return  RSA Hex 格式密钥
     */
    public synchronized String getRsaHexKeyOrGenerate() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        String rsaHexKey = systemConfigDTO.getRsaHexKey();
        if (StrUtil.isEmpty(rsaHexKey)) {
            byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
            rsaHexKey = HexUtil.encodeHexStr(key);
            
            SystemConfig loginVerifyModeConfig = systemConfigMapper.findByName(SystemConfigConstant.RSA_HEX_KEY);
            loginVerifyModeConfig.setValue(rsaHexKey);
            systemConfigMapper.updateById(loginVerifyModeConfig);
            systemConfigDTO.setRsaHexKey(rsaHexKey);
            
            Cache cache = cacheManager.getCache(CACHE_NAME);
            Optional.ofNullable(cache).ifPresent(cache1 -> cache1.put("dto", systemConfigDTO));
        }
        return rsaHexKey;
    }
    
    
    /**
     * 获取系统是否已初始化
     *
     * @return  管理员名称
     */
    public Boolean getSystemIsInstalled() {
        return systemConfigService.getSystemConfig().getInstalled();
    }
    
    
    /**
     * 获取后端站点域名
     *
     * @return  后端站点域名
     */
    public String getDomain() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        return systemConfigDTO.getDomain();
    }
    
    
    /**
     * 获取前端站点域名
     *
     * @return  前端站点域名
     */
    public String getFrontDomain() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        return systemConfigDTO.getFrontDomain();
    }
    
    
    /**
     * 获取实际的前端站点域名
     *
     * @return  实际的前端站点域名
     */
    public String getRealFrontDomain() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        return StrUtil.firstNonNull(systemConfigDTO.getFrontDomain(), systemConfigDTO.getDomain());
    }
    
    
    /**
     * 获取前端地址下的 403 页面地址.
     *
     * @return 前端地址下的 403 页面地址.
     *
     */
    public String getForbiddenUrl() {
        return getRealFrontDomain() + "/403";
    }

}