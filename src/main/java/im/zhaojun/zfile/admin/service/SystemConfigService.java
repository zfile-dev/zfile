package im.zhaojun.zfile.admin.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.constant.SystemConfigConstant;
import im.zhaojun.zfile.admin.mapper.SystemConfigMapper;
import im.zhaojun.zfile.admin.model.entity.SystemConfig;
import im.zhaojun.zfile.admin.model.enums.LoginVerifyModeEnum;
import im.zhaojun.zfile.common.cache.ZFileCache;
import im.zhaojun.zfile.common.util.EnumConvertUtils;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统设置 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
@DependsOn("zFileCache")
public class SystemConfigService extends ServiceImpl<SystemConfigMapper, SystemConfig> {

    private static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "123456";

    private static final LoginVerifyModeEnum DEFAULT_LOGIN_VERIFY_MODE = LoginVerifyModeEnum.IMG_VERIFY_MODE;

    @Resource
    private ZFileCache zFileCache;

    @Resource
    private SystemConfigMapper systemConfigMapper;

    private final Class<SystemConfigDTO> systemConfigClazz = SystemConfigDTO.class;

    /**
     * 获取系统设置, 如果缓存中有, 则去缓存取, 没有则查询数据库并写入到缓存中.
     *
     * @return  系统设置
     */
    public SystemConfigDTO getSystemConfig() {
        SystemConfigDTO cacheConfig = zFileCache.getConfig();
        if (cacheConfig != null) {
            return cacheConfig;
        }

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

        zFileCache.updateConfig(systemConfigDTO);
        return systemConfigDTO;
    }


    /**
     * 更新系统设置, 并清空缓存中的内容.
     *
     * @param   systemConfigDTO
     *          系统设置 dto
     */
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

        zFileCache.removeConfig();

        systemConfigList.forEach(systemConfig -> systemConfigMapper.updateById(systemConfig));
    }


    /**
     * 重置管理员登录信息, 重置登录账号为 admin, 密码为 123456, 登录校验方式为 图形验证码.
     */
    public void resetAdminLoginInfo() {
        SystemConfig usernameConfig = systemConfigMapper.findByName(SystemConfigConstant.USERNAME);
        usernameConfig.setValue(DEFAULT_USERNAME);
        saveOrUpdate(usernameConfig);

        String encryptionPassword = SecureUtil.md5(DEFAULT_PASSWORD);
        SystemConfig passwordConfig = systemConfigMapper.findByName(SystemConfigConstant.PASSWORD);
        passwordConfig.setValue(encryptionPassword);
        saveOrUpdate(passwordConfig);

        SystemConfig loginVerifyModeConfig = systemConfigMapper.findByName(SystemConfigConstant.LOGIN_VERIFY_MODE);
        loginVerifyModeConfig.setValue(DEFAULT_LOGIN_VERIFY_MODE.getValue());
        saveOrUpdate(loginVerifyModeConfig);

        zFileCache.removeConfig();
    }


    /**
     * 获取是否已安装初始化
     *
     * @return  是否已安装初始化
     */
    public boolean getIsInstall() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return StrUtil.isNotEmpty(systemConfigDTO.getUsername());
    }


    /**
     * 获取管理员名称
     *
     * @return  管理员名称
     */
    public String getAdminUsername() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getUsername();
    }


    /**
     * 获取系统是否已初始化
     *
     * @return  管理员名称
     */
    public Boolean getSystemIsInstalled() {
        return getSystemConfig().getInstalled();
    }


    /**
     * 获取后端站点域名
     *
     * @return  后端站点域名
     */
    public String getDomain() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getDomain();
    }


    /**
     * 获取前端站点域名
     *
     * @return  前端站点域名
     */
    public String getFrontDomain() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getFrontDomain();
    }


    /**
     * 获取实际的前端站点域名
     *
     * @return  实际的前端站点域名
     */
    public String getRealFrontDomain() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();

        String baseUrl = "";

        if (StrUtil.isNotEmpty(systemConfigDTO.getFrontDomain())) {
            baseUrl = systemConfigDTO.getFrontDomain();
        } else if (StrUtil.isNotEmpty(systemConfigDTO.getDomain())) {
            baseUrl = systemConfigDTO.getDomain();
        }

        return baseUrl;
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


    /**
     * 获取直链前缀
     *
     * @return  直链前缀
     */
    public String getDirectLinkPrefix() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        return systemConfigDTO.getDirectLinkPrefix();
    }


    /**
     * 获取 RSA Hex 格式密钥
     *
     * @return  RSA Hex 格式密钥
     */
    public String getRsaHexKey() {
        SystemConfigDTO systemConfigDTO = getSystemConfig();
        String rsaHexKey = systemConfigDTO.getRsaHexKey();
        if (StrUtil.isEmpty(rsaHexKey)) {
            byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
            rsaHexKey = HexUtil.encodeHexStr(key);

            SystemConfig loginVerifyModeConfig = systemConfigMapper.findByName(SystemConfigConstant.RSA_HEX_KEY);
            loginVerifyModeConfig.setValue(rsaHexKey);
            saveOrUpdate(loginVerifyModeConfig);

            systemConfigDTO.setRsaHexKey(rsaHexKey);
            zFileCache.updateConfig(systemConfigDTO);
        }
        return rsaHexKey;
    }

}