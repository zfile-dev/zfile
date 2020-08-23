package im.zhaojun.zfile.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.crypto.SecureUtil;
import im.zhaojun.zfile.cache.ZFileCache;
import im.zhaojun.zfile.exception.InvalidDriveException;
import im.zhaojun.zfile.model.constant.SystemConfigConstant;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.model.dto.SystemFrontConfigDTO;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.entity.SystemConfig;
import im.zhaojun.zfile.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    @Resource
    private ZFileCache zFileCache;

    @Resource
    private SystemConfigRepository systemConfigRepository;

    @Resource
    private DriveConfigService driveConfigService;

    private Class<SystemConfigDTO> systemConfigClazz = SystemConfigDTO.class;


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
        List<SystemConfig> systemConfigList = systemConfigRepository.findAll();

        for (SystemConfig systemConfig : systemConfigList) {
            String key = systemConfig.getKey();

            try {
                Field field = systemConfigClazz.getDeclaredField(key);
                field.setAccessible(true);
                String strVal = systemConfig.getValue();
                Object convertVal = Convert.convert(field.getType(), strVal);
                field.set(systemConfigDTO, convertVal);
            } catch (NoSuchFieldException | IllegalAccessException e) {
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
     *          系统
     *
     */
    public void updateSystemConfig(SystemConfigDTO systemConfigDTO) {
        List<SystemConfig> systemConfigList = new ArrayList<>();

        Field[] fields = systemConfigClazz.getDeclaredFields();
        for (Field field : fields) {
            String key = field.getName();
            SystemConfig systemConfig = systemConfigRepository.findByKey(key);
            if (systemConfig != null) {
                field.setAccessible(true);
                Object val = null;

                try {
                    val = field.get(systemConfigDTO);
                } catch (IllegalAccessException e) {
                    log.error("通过反射, 从 SystemConfigDTO 获取字段 {}  时出现异常:", key, e);
                }

                if (val != null) {
                    systemConfig.setValue(val.toString());
                    systemConfigList.add(systemConfig);
                }
            }
        }

        zFileCache.removeConfig();
        systemConfigRepository.saveAll(systemConfigList);
    }


    /**
     * 根据驱动器 ID, 获取对于前台页面的系统设置.
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  前台系统设置
     */
    public SystemFrontConfigDTO getSystemFrontConfig(Integer driveId) {
        SystemConfigDTO systemConfig = getSystemConfig();
        SystemFrontConfigDTO systemFrontConfigDTO = new SystemFrontConfigDTO();
        BeanUtils.copyProperties(systemConfig, systemFrontConfigDTO);

        DriveConfig driveConfig = driveConfigService.findById(driveId);
        if (driveConfig == null) {
            throw new InvalidDriveException("此驱动器不存在或初始化失败, 请检查后台参数配置");
        }
        systemFrontConfigDTO.setSearchEnable(driveConfig.getSearchEnable());
        return systemFrontConfigDTO;
    }


    /**
     * 更新后台账号密码
     *
     * @param   username
     *          用户名
     *
     * @param   password
     *          密码
     */
    public void updateUsernameAndPwd(String username, String password) {
        SystemConfig usernameConfig = systemConfigRepository.findByKey(SystemConfigConstant.USERNAME);
        usernameConfig.setValue(username);
        systemConfigRepository.save(usernameConfig);

        String encryptionPassword = SecureUtil.md5(password);
        SystemConfig systemConfig = systemConfigRepository.findByKey(SystemConfigConstant.PASSWORD);
        systemConfig.setValue(encryptionPassword);

        zFileCache.removeConfig();

        systemConfigRepository.save(systemConfig);
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

}