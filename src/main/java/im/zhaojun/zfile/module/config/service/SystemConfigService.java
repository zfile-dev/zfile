package im.zhaojun.zfile.module.config.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.util.*;
import im.zhaojun.zfile.module.config.annotation.JSONStringParse;
import im.zhaojun.zfile.module.config.constant.SystemConfigConstant;
import im.zhaojun.zfile.module.config.event.SystemConfigModifyHandlerChain;
import im.zhaojun.zfile.module.config.mapper.SystemConfigMapper;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.user.model.enums.LoginVerifyModeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

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

    private static final String SERIAL_VERSION_UID_FIELD_NAME = "serialVersionUID";

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private SystemConfigModifyHandlerChain systemConfigModifyHandlerChain;

    private final Class<SystemConfigDTO> systemConfigClazz = SystemConfigDTO.class;

    public static final List<String> ignoreFieldList = Arrays.asList("domain");

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
            if (ignoreFieldList.contains(key)) {
                if (log.isTraceEnabled()) {
                    log.trace("从数据库加载字段填充到 DTO 时，忽略字段: {}", key);
                }
                continue;
            }
            try {
                Field field = systemConfigClazz.getDeclaredField(key);
                field.setAccessible(true);
                String strVal = systemConfig.getValue();
                Class<?> fieldType = field.getType();

                Object convertVal;
                if (EnumUtil.isEnum(fieldType)) {
                    convertVal = EnumConvertUtils.convertStrToEnum(fieldType, strVal);
                } else if (field.isAnnotationPresent(JSONStringParse.class)) {
                    // 如果类是 Collection 类型, 则需要将 JSON 字符串转换为 List
                    if (Collection.class.isAssignableFrom(fieldType)) {
                        Class<?> genericType = ClassUtils.getGenericType(field);
                        convertVal = JSONArray.parseArray(strVal, genericType);
                    } else {
                        // 否则转换为普通对象
                        convertVal = JSONObject.parseObject(strVal, fieldType);
                    }
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
    public synchronized void updateSystemConfig(SystemConfigDTO systemConfigDTO) {
        // 获取更新前的值
        List<SystemConfig> systemConfigListInDb = systemConfigMapper.findAll();
        Map<String, SystemConfig> systemConfigMapInDb = CollectionUtils.toMap(systemConfigListInDb, null, SystemConfig::getName);

        // 存储更新后的值
        List<SystemConfig> updateSystemConfigList = new ArrayList<>();

        Field[] fields = systemConfigClazz.getDeclaredFields();
        for (Field field : fields) {
            // 获取数据库中的值对象
            String key = field.getName();
            if (SERIAL_VERSION_UID_FIELD_NAME.equals(key)) {
                continue;
            }
            SystemConfig systemConfig = systemConfigMapInDb.get(key);
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
                    } else if (field.isAnnotationPresent(JSONStringParse.class)) {
                        val = JSONObject.toJSONString(val);
                    }
                    // 如果和原来的值一样, 则跳过
                    String originVal = systemConfig.getValue();
                    if (ObjUtil.equals(originVal, val)) {
                        continue;
                    }
                    // 将更新后的值存到更新列表中
                    SystemConfig updateSystemConfig = new SystemConfig();
                    updateSystemConfig.setId(systemConfig.getId());
                    updateSystemConfig.setName(systemConfig.getName());
                    updateSystemConfig.setValue(Convert.toStr(val));
                    updateSystemConfig.setTitle(systemConfig.getTitle());
                    updateSystemConfigList.add(updateSystemConfig);
                }
            } else {
                log.warn("尝试保存系统配置表中不存在字段: {}", key);
            }
        }

        updateSystemConfigList.forEach(systemConfigInForm -> {
            SystemConfig systemConfigInDb = systemConfigMapInDb.get(systemConfigInForm.getName());
            systemConfigModifyHandlerChain.execute(systemConfigInDb, systemConfigInForm);
            systemConfigMapper.updateById(systemConfigInForm);
        });
    }


    /**
     * 获取 AES Hex 格式密钥
     *
     * @return  AES Hex 格式密钥
     */
    public synchronized String getAesHexKeyOrGenerate() {
        SystemConfigDTO systemConfigDTO = ((SystemConfigService)AopContext.currentProxy()).getSystemConfig();
        String aesHexKey = systemConfigDTO.getRsaHexKey();
        if (StringUtils.isEmpty(aesHexKey)) {
            byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
            aesHexKey = HexUtil.encodeHexStr(key);

            SystemConfig loginVerifyModeConfig = systemConfigMapper.findByName(SystemConfigConstant.AES_HEX_KEY);
            loginVerifyModeConfig.setValue(aesHexKey);
            systemConfigMapper.updateById(loginVerifyModeConfig);
            systemConfigDTO.setRsaHexKey(aesHexKey);

            Cache cache = cacheManager.getCache(CACHE_NAME);
            Optional.ofNullable(cache).ifPresent(cache1 -> cache1.put("dto", systemConfigDTO));
        }
        return aesHexKey;
    }


    /**
     * 获取前端站点域名
     *
     * @return  前端站点域名
     */
    public String getFrontDomain() {
        SystemConfigDTO systemConfigDTO = ((SystemConfigService)AopContext.currentProxy()).getSystemConfig();
        return systemConfigDTO.getFrontDomain();
    }


    /**
     * 获取实际的前端站点域名
     *
     * @return  实际的前端站点域名
     */
    public String getRealFrontDomain() {
        SystemConfigDTO systemConfigDTO = ((SystemConfigService)AopContext.currentProxy()).getSystemConfig();
        return StringUtils.firstNonNull(systemConfigDTO.getFrontDomain(), getAxiosFromDomainOrSetting(), RequestHolder.getOriginAddress());
    }


    /**
     * 优先级：
     * 1. 如果设置了强制后端地址，则使用强制后端地址。
     * 2. 如果请求中有 axios-from 参数，则使用该参数。
     * 3. 如果没有强制后端地址和 axios-from 参数，则使用请求的服务器地址（如果经过多个代理，可能不是实际的后端地址）。
     *
     * @return  后端站点地址
     */
    public String getAxiosFromDomainOrSetting() {
        SystemConfigDTO systemConfigDTO = ((SystemConfigService)AopContext.currentProxy()).getSystemConfig();
        if (StringUtils.isNotBlank(systemConfigDTO.getForceBackendAddress())) {
            return systemConfigDTO.getForceBackendAddress();
        } else if (StringUtils.isNotEmpty(RequestHolder.getAxiosFrom())) {
            return RequestHolder.getAxiosFrom();
        } else {
            return RequestHolder.getRequestServerAddress();
        }
    }

    /**
     * 获取前端地址下的 401 页面地址.
     *
     * @return 前端地址下的 401 页面地址.
     *
     */
    public String getUnauthorizedUrl() {
        return getUnauthorizedUrl(null, null);
    }

    /**
     * 获取前端地址下的 401 页面地址. 可以指定 code 和 message.
     *
     * @param   code
     *          指定错误码
     *
     * @param   message
     *          指定错误信息
     *
     * @return 前端地址下的 401 页面地址.
     */
    public String getUnauthorizedUrl(String code, String message) {
        String url = StringUtils.concat(getRealFrontDomain(), "/401");
        UrlBuilder urlBuilder = UrlBuilder.of(url);
        if (StringUtils.isNotBlank(code)) {
            urlBuilder.addQuery("code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            urlBuilder.addQuery("message", message);
        }
        return urlBuilder.build();
    }

    /**
     * 获取前端地址下的 403 页面地址.
     *
     * @return 前端地址下的 403 页面地址.
     *
     */
    public String getForbiddenUrl() {
        return getForbiddenUrl(null, null);
    }

    /**
     * 获取前端地址下的 403 页面地址. 可以指定 code 和 message.
     *
     * @param   code
     *          指定错误码
     *
     * @param   message
     *          指定错误信息
     *
     * @return 前端地址下的 403 页面地址.
     */
    public String getForbiddenUrl(String code, String message) {
        String url = StringUtils.concat(getRealFrontDomain(), "/403");
        UrlBuilder urlBuilder = UrlBuilder.of(url);
        if (StringUtils.isNotBlank(code)) {
            urlBuilder.addQuery("code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            urlBuilder.addQuery("message", message);
        }
        return urlBuilder.build();
    }

    /**
     * 获取前端地址下的 404 页面地址.
     *
     * @return 前端地址下的 404 页面地址.
     *
     */
    public String getNotFoundUrl() {
        return getNotFoundUrl(null, null);
    }

    /**
     * 获取前端地址下的 404 页面地址. 可以指定 code 和 message.
     *
     * @param   code
     *          指定错误码
     *
     * @param   message
     *          指定错误信息
     *
     * @return 前端地址下的 404 页面地址.
     */
    public String getNotFoundUrl(String code, String message) {
        String url = StringUtils.concat(getRealFrontDomain(), "/404");
        UrlBuilder urlBuilder = UrlBuilder.of(url);
        if (StringUtils.isNotBlank(code)) {
            urlBuilder.addQuery("code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            urlBuilder.addQuery("message", message);
        }
        return urlBuilder.build();
    }

    /**
     * 获取前端地址下的 500 页面地址. 可以指定 code 和 message.
     *
     * @param   code
     *          指定错误码
     *
     * @param   message
     *          指定错误信息
     *
     * @return 前端地址下的 500 页面地址.
     */
    public String getErrorPageUrl(String code, String message) {
        String url = StringUtils.concat(getRealFrontDomain(), "/500");
        UrlBuilder urlBuilder = UrlBuilder.of(url);
        if (StringUtils.isNotBlank(code)) {
            urlBuilder.addQuery("code", code);
        }
        if (StringUtils.isNotBlank(message)) {
            urlBuilder.addQuery("message", message);
        }
        return urlBuilder.build();
    }


    /**
     * 重置登录验证模式，去除所有登录额外验证方式.
     */
    public void resetLoginVerifyMode() {
        SystemConfigDTO systemConfigDTO = ((SystemConfigService)AopContext.currentProxy()).getSystemConfig();
        systemConfigDTO.setLoginImgVerify(false);
        systemConfigDTO.setAdminTwoFactorVerify(false);
        systemConfigDTO.setLoginVerifySecret("");
        systemConfigDTO.setLoginVerifyMode(LoginVerifyModeEnum.OFF_MODE);
        ((SystemConfigService)AopContext.currentProxy()).updateSystemConfig(systemConfigDTO);
    }

}