package im.zhaojun.zfile.service;

import im.zhaojun.zfile.context.StorageTypeContext;
import im.zhaojun.zfile.exception.InitializeException;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.dto.DriveConfigDTO;
import im.zhaojun.zfile.model.dto.StorageStrategyConfig;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.repository.DriverConfigRepository;
import im.zhaojun.zfile.repository.StorageConfigRepository;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.context.DriveContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 驱动器 Service 类
 * @author zhaojun
 */
@Slf4j
@Service
public class DriveConfigService {

    @Resource
    private DriverConfigRepository driverConfigRepository;

    @Resource
    private StorageConfigRepository storageConfigRepository;

    @Resource
    private DriveContext driveContext;

    public static final Class<StorageStrategyConfig> STORAGE_STRATEGY_CONFIG_CLASS = StorageStrategyConfig.class;

    /**
     * 获取所有驱动器列表
     * 
     * @return  驱动器列表
     */
    public List<DriveConfig> list() {
        return driverConfigRepository.findAll();
    }


    /**
     * 获取指定驱动器设置
     *
     * @param   id
     *          驱动器 ID
     *
     * @return  驱动器设置
     */
    public DriveConfig findById(Integer id) {
        return driverConfigRepository.getOne(id);
    }


    /**
     * 获取指定驱动器 DTO 对象, 此对象包含详细的参数设置.
     *
     * @param   id
     *          驱动器 ID
     *
     * @return  驱动器 DTO
     */
    public DriveConfigDTO findDriveConfigDTOById(Integer id) {
        DriveConfig driveConfig = driverConfigRepository.getOne(id);

        DriveConfigDTO driveConfigDTO = new DriveConfigDTO();

        List<StorageConfig> storageConfigList = storageConfigRepository.findByDriveId(driveConfig.getId());
        BeanUtils.copyProperties(driveConfig, driveConfigDTO);

        StorageStrategyConfig storageStrategyConfig = new StorageStrategyConfig();
        for (StorageConfig storageConfig : storageConfigList) {
            String key = storageConfig.getKey();
            String value = storageConfig.getValue();

            Field declaredField;
            try {
                declaredField = STORAGE_STRATEGY_CONFIG_CLASS.getDeclaredField(key);
                declaredField.setAccessible(true);
                if (Objects.equals(StorageConfigConstant.IS_PRIVATE, key)) {
                    declaredField.set(storageStrategyConfig, Boolean.valueOf(value));
                } else {
                    declaredField.set(storageStrategyConfig, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                if (log.isDebugEnabled()) {
                    log.debug("通过反射, 将字段 {" + key + "}注入 DriveConfigDTO 时出现异常:", e);
                }
            }

        }

        driveConfigDTO.setStorageStrategyConfig(storageStrategyConfig);
        return driveConfigDTO;
    }

    /**
     * 获取指定驱动器的存储策略.
     *
     * @param   id
     *          驱动器 ID
     *
     * @return  驱动器对应的存储策略.
     */
    public StorageTypeEnum findStorageTypeById(Integer id) {
        // return findById(id).getType();
        return driverConfigRepository.findById(id).get().getType();
    }


    /**
     * 保存驱动器基本信息及其对应的参数设置
     *
     * @param driveConfigDTO    驱动器 DTO 对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(DriveConfigDTO driveConfigDTO) {

        // 判断是新增还是修改
        boolean updateFlag = driveConfigDTO.getId() != null;

        // 保存基本信息
        DriveConfig driveConfig = new DriveConfig();
        StorageTypeEnum storageType = driveConfigDTO.getType();
        BeanUtils.copyProperties(driveConfigDTO, driveConfig);
        driverConfigRepository.save(driveConfig);

        // 保存存储策略设置.
        StorageStrategyConfig storageStrategyConfig = driveConfigDTO.getStorageStrategyConfig();

        AbstractBaseFileService storageTypeService = StorageTypeContext.getStorageTypeService(storageType);

        List<StorageConfig> storageConfigList;
        if (updateFlag) {
            storageConfigList = storageConfigRepository.findByDriveId(driveConfigDTO.getId());
        } else {
            storageConfigList = storageTypeService.storageStrategyConfigList();
        }

        for (StorageConfig storageConfig : storageConfigList) {
            String key = storageConfig.getKey();

            try {
                Field field = STORAGE_STRATEGY_CONFIG_CLASS.getDeclaredField(key);
                field.setAccessible(true);
                Object o = field.get(storageStrategyConfig);
                String value = o == null ? null : o.toString();
                storageConfig.setValue(value);
                storageConfig.setType(storageType);
                storageConfig.setDriveId(driveConfig.getId());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                if (log.isDebugEnabled()) {
                    log.debug("通过反射, 从 StorageStrategyConfig 中获取字段 {" + key + "} 时出现异常:", e);
                }
            }

        }
        storageConfigRepository.saveAll(storageConfigList);

        driveContext.initDrive(driveConfig.getId());

        AbstractBaseFileService driveService = driveContext.getDriveService(driveConfig.getId());
        if (driveService.getIsUnInitialized()) {
            throw new InitializeException("初始化异常, 请检查配置是否正确.");
        }
    }


    /**
     * 删除指定驱动器设置, 会级联删除其参数设置
     *
     * @param   id
     *          驱动器 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        driverConfigRepository.deleteById(id);
        storageConfigRepository.deleteByDriveId(id);
        driveContext.destroyDrive(id);
    }


    /**
     * 根据存储策略类型获取所有驱动器
     *
     * @param   type
     *          存储类型
     *
     * @return  指定存储类型的存储器
     */
    public List<DriveConfig> findByType(StorageTypeEnum type) {
        return driverConfigRepository.findByType(type);
    }


    /**
     * 更新指定驱动器的缓存启用状态
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   cacheEnable
     *          是否启用缓存
     */
    public void updateCacheStatus(Integer driveId, Boolean cacheEnable) {
        DriveConfig driveConfig = findById(driveId);
        if (driveConfig != null) {
            driveConfig.setEnableCache(cacheEnable);
            driverConfigRepository.save(driveConfig);
        }
    }
}