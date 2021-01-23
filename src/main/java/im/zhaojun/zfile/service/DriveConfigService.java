package im.zhaojun.zfile.service;

import com.alibaba.fastjson.JSONObject;
import im.zhaojun.zfile.cache.ZFileCache;
import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.context.StorageTypeContext;
import im.zhaojun.zfile.exception.InitializeDriveException;
import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.dto.CacheInfoDTO;
import im.zhaojun.zfile.model.dto.DriveConfigDTO;
import im.zhaojun.zfile.model.dto.StorageStrategyConfig;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.repository.DriverConfigRepository;
import im.zhaojun.zfile.repository.StorageConfigRepository;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @Resource
    private ZFileCache zFileCache;

    public static final Class<StorageStrategyConfig> STORAGE_STRATEGY_CONFIG_CLASS = StorageStrategyConfig.class;


    /**
     * 获取所有驱动器列表
     * 
     * @return  驱动器列表
     */
    public List<DriveConfig> list() {
        Sort sort = new Sort(Sort.Direction.ASC,"orderNum");
        return driverConfigRepository.findAll(sort);
    }


    /**
     * 获取所有已启用的驱动器列表
     *
     * @return  已启用的驱动器列表
     */
    public List<DriveConfig> listOnlyEnable() {
        DriveConfig driveConfig = new DriveConfig();
        driveConfig.setEnable(true);
        Example<DriveConfig> example = Example.of(driveConfig);
        Sort sort = new Sort(Sort.Direction.ASC,"orderNum");
        return driverConfigRepository.findAll(example, sort);
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
        return driverConfigRepository.findById(id).orElse(null);
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
                log.error("通过反射, 将字段 {} 注入 DriveConfigDTO 时出现异常:", key, e);
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
        return driverConfigRepository.findById(id).get().getType();
    }


    /**
     * 更新驱动器设置
     * @param driveConfig   驱动器设置
     */
    public void updateDriveConfig(DriveConfig driveConfig) {
        driverConfigRepository.save(driveConfig);
    }


    /**
     * 保存驱动器基本信息及其对应的参数设置
     *
     * @param driveConfigDTO    驱动器 DTO 对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDriveConfigDTO(DriveConfigDTO driveConfigDTO) {

        // 判断是新增还是修改
        boolean updateFlag = driveConfigDTO.getId() != null;

        // 保存基本信息
        DriveConfig driveConfig = new DriveConfig();
        StorageTypeEnum storageType = driveConfigDTO.getType();
        BeanUtils.copyProperties(driveConfigDTO, driveConfig);

        if (driveConfig.getId() == null) {
            Integer nextId = selectNextId();
            driveConfig.setId(nextId);
        }
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
                log.error("通过反射, 从 StorageStrategyConfig 中获取字段 {} 时出现异常:", key, e);
            }

        }
        storageConfigRepository.saveAll(storageConfigList);

        driveContext.init(driveConfig.getId());

        AbstractBaseFileService driveService = driveContext.get(driveConfig.getId());
        if (driveService.getIsUnInitialized()) {
            throw new InitializeDriveException("初始化异常, 请检查配置是否正确.");
        }

        if (driveConfig.getAutoRefreshCache()) {
            startAutoCacheRefresh(driveConfig.getId());
        } else if (updateFlag){
            stopAutoCacheRefresh(driveConfig.getId());
        }

    }


    /**
     * 查询驱动器最大的 ID
     *
     * @return  驱动器最大 ID
     */
    public Integer selectNextId() {
        Integer maxId = driverConfigRepository.selectMaxId();
        if (maxId == null) {
            maxId = 1;
        }

        return maxId + 1;
    }


    /**
     * 更新驱动器 ID
     *
     * @param   updateId
     *          驱动器原 ID
     *
     * @param   newId
     *          驱动器新 ID
     */
    @Transactional
    public void updateId(Integer updateId, Integer newId) {
        driverConfigRepository.updateId(updateId, newId);
        storageConfigRepository.updateDriveId(updateId, newId);
        driveContext.updateDriveId(updateId, newId);
    }


    /**
     * 删除指定驱动器设置, 会级联删除其参数设置
     *
     * @param   id
     *          驱动器 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        if (log.isDebugEnabled()) {
            log.debug("尝试删除驱动器, driveId: {}", id);
        }
        DriveConfig driveConfig = driverConfigRepository.getOne(id);
        driverConfigRepository.deleteById(id);
        storageConfigRepository.deleteByDriveId(id);
        if (driveConfig.getEnableCache()) {
            zFileCache.stopAutoCacheRefresh(id);
            zFileCache.clear(id);
        }
        driveContext.destroy(id);
        if (log.isDebugEnabled()) {
            log.debug("尝试删除驱动器成功, 已清理相关数据, driveId: {}", id);
        }
    }


    /**
     * 根据存储策略类型获取所有驱动器
     *
     * @param   type
     *          存储类型
     *
     * @return  指定存储类型的驱动器
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


    /**
     * 获取指定驱动器的缓存信息
     * @param   driveId
     *          驱动器 ID
     * @return  缓存信息
     */
    public CacheInfoDTO findCacheInfo(Integer driveId) {
        int hitCount = zFileCache.getHitCount(driveId);
        int missCount = zFileCache.getMissCount(driveId);
        Set<String> keys = zFileCache.keySet(driveId);
        int cacheCount = keys.size();
        return new CacheInfoDTO(cacheCount, hitCount, missCount, keys);
    }


    /**
     * 刷新指定 key 的缓存:
     *  1. 清空此 key 的缓存.
     *  2. 重新调用方法写入缓存.
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @param   key
     *          缓存 key (文件夹名称)
     */
    public void refreshCache(Integer driveId, String key) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("手动刷新缓存 driveId: {}, key: {}", driveId, key);
        }
        zFileCache.remove(driveId, key);
        AbstractBaseFileService baseFileService = driveContext.get(driveId);
        baseFileService.fileList(key);
    }


    /**
     * 开启缓存自动刷新
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void startAutoCacheRefresh(Integer driveId) {
        DriveConfig driveConfig = findById(driveId);
        driveConfig.setAutoRefreshCache(true);
        driverConfigRepository.save(driveConfig);
        zFileCache.startAutoCacheRefresh(driveId);
    }


    /**
     * 停止缓存自动刷新
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void stopAutoCacheRefresh(Integer driveId) {
        DriveConfig driveConfig = findById(driveId);
        driveConfig.setAutoRefreshCache(false);
        driverConfigRepository.save(driveConfig);
        zFileCache.stopAutoCacheRefresh(driveId);
    }

    /**
     * 清理缓存
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void clearCache(Integer driveId) {
        zFileCache.clear(driveId);
    }


    /**
     * 交换驱动器排序
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDriveDrag(List<JSONObject> driveConfigs) {
        for (int i = 0; i < driveConfigs.size(); i++) {
            JSONObject item = driveConfigs.get(i);
            driverConfigRepository.updateSetOrderNumById(i, item.getInteger("id"));
        }
    }

}