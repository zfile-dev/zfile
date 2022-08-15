package im.zhaojun.zfile.admin.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.annotation.model.StorageSourceParamDef;
import im.zhaojun.zfile.admin.mapper.StorageSourceMapper;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.admin.model.param.IStorageParam;
import im.zhaojun.zfile.admin.model.request.SaveStorageSourceRequest;
import im.zhaojun.zfile.common.cache.ZFileCache;
import im.zhaojun.zfile.common.context.StorageSourceContext;
import im.zhaojun.zfile.common.exception.InitializeStorageSourceException;
import im.zhaojun.zfile.common.exception.InvalidStorageSourceException;
import im.zhaojun.zfile.home.model.dto.CacheInfoDTO;
import im.zhaojun.zfile.home.model.dto.StorageSourceAllParam;
import im.zhaojun.zfile.home.model.dto.StorageSourceDTO;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.home.model.request.UpdateStorageSortRequest;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 存储源基本信息 Service
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class StorageSourceService extends ServiceImpl<StorageSourceMapper, StorageSource> implements IService<StorageSource> {

    @Resource
    private StorageSourceMapper storageSourceMapper;

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    @Resource
    private StorageSourceContext storageSourceContext;

    @Resource
    private ZFileCache zFileCache;

    public static final Class<StorageSourceAllParam> STORAGE_SOURCE_ALL_PARAM_CLASS = StorageSourceAllParam.class;


    /**
     * 存储源 ID -> 存储源对象缓存
     */
    private final Map<Integer, StorageSource> storageIdMapCache = new HashMap<>();


    /**
     * 存储源 KEY -> 存储源对象缓存
     */
    private final Map<String, StorageSource> storageKeyMapCache = new HashMap<>();


    /**
     * 获取所有存储源列表
     *
     * @return 存储源列表
     */
    public List<StorageSource> findAllOrderByOrderNum() {
        return storageSourceMapper.findAllOrderByOrderNum();
    }


    /**
     * 获取所有已启用的存储源列表，按照存储源的排序号排序
     *
     * @return 已启用的存储源列表
     */
    public List<StorageSource> findListByEnableOrderByOrderNum() {
        return storageSourceMapper.findListByEnableOrderByOrderNum();
    }


    /**
     * 获取指定存储源设置
     *
     * @param   id
     *          存储源 ID
     *
     * @return  存储源设置
     */
    public StorageSource findById(Integer id) {
        if (storageIdMapCache.get(id) != null) {
            return storageIdMapCache.get(id);
        } else {
            StorageSource dbResult = storageSourceMapper.selectById(id);
            storageIdMapCache.put(id, dbResult);
            return dbResult;
        }
    }


    /**
     * 获取指定存储源 DTO 对象, 此对象包含详细的参数设置.
     *
     * @param   id
     *          存储源 ID
     *
     * @return  存储源 DTO
     */
    public StorageSourceDTO findStorageSourceDTOById(Integer id) {
        StorageSource storageSource = findById(id);
        Boolean defaultSwitchToImgMode = storageSource.getDefaultSwitchToImgMode();
        if (defaultSwitchToImgMode == null) {
            storageSource.setDefaultSwitchToImgMode(false);
        }

        StorageSourceDTO storageSourceDTO = new StorageSourceDTO();
        BeanUtils.copyProperties(storageSource, storageSourceDTO);

        List<StorageSourceConfig> storageSourceConfigList = storageSourceConfigService.selectStorageConfigByStorageId(storageSource.getId());

        StorageSourceAllParam storageSourceAllParam = new StorageSourceAllParam();
        for (StorageSourceConfig storageSourceConfig : storageSourceConfigList) {
            String name = storageSourceConfig.getName();
            String value = storageSourceConfig.getValue();

            Field declaredField;
            try {
                declaredField = STORAGE_SOURCE_ALL_PARAM_CLASS.getDeclaredField(name);
                declaredField.setAccessible(true);

                Class<?> paramFieldType = declaredField.getType();
                Object convertToValue = Convert.convert(paramFieldType, value);
                declaredField.set(storageSourceAllParam, convertToValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("通过反射, 将字段 {} 注入 StorageSourceDTO 时出现异常:", name, e);
            }
        }

        storageSourceDTO.setStorageSourceAllParam(storageSourceAllParam);
        return storageSourceDTO;
    }


    /**
     * 根据 id 获取指定存储源的类型.
     *
     * @param   id
     *          存储源 ID
     *
     * @return  存储源对应的类型.
     */
    public StorageTypeEnum findStorageTypeById(Integer id) {
        return findById(id).getType();
    }


    /**
     * 保存存储源基本信息及其对应的参数设置
     *
     * @param   saveStorageSourceRequest
     *          存储源 DTO 对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveStorageSource(SaveStorageSourceRequest saveStorageSourceRequest) {

        // 判断是新增还是修改
        boolean updateFlag = saveStorageSourceRequest.getId() != null;

        // 保存基本信息
        StorageSource storageSource = new StorageSource();
        StorageTypeEnum storageType = saveStorageSourceRequest.getType();
        BeanUtils.copyProperties(saveStorageSourceRequest, storageSource);

        if (storageSource.getId() == null) {
            Integer nextId = selectNextId();
            storageSource.setId(nextId);
        }


        // 获取通过 id 缓存的对象
        StorageSource cacheStorageSource = storageIdMapCache.get(storageSource.getId());
        // 如果缓存的对象和当前的对象 key 不同, 则说明修改了 key, 需要移除 key 缓存, 并重新写入.
        if (ObjectUtil.isNotEmpty(cacheStorageSource) &&
                !StrUtil.equals(cacheStorageSource.getKey(), storageSource.getKey())) {
            storageKeyMapCache.remove(cacheStorageSource.getKey());
        }

        super.saveOrUpdate(storageSource);

        if (StrUtil.isEmpty(storageSource.getKey()) && !StrUtil.equals(storageSource.getId().toString(), storageSource.getKey())) {
            storageSource.setKey(Convert.toStr(storageSource.getId()));
            baseMapper.updateById(storageSource);
        }
        storageKeyMapCache.put(storageSource.getKey(), storageSource);

        StorageSourceAllParam storageSourceAllParam = saveStorageSourceRequest.getStorageSourceAllParam();

        // 获取该存储源类型需要的参数列表
        List<StorageSourceParamDef> storageSourceParamList = StorageSourceContext.getStorageSourceParamListByType(storageType);

        List<StorageSourceConfig> storageSourceConfigList = new ArrayList<>();
        storageSourceConfigService.deleteByStorageId(saveStorageSourceRequest.getId());

        for (StorageSourceParamDef storageSourceParam : storageSourceParamList) {
            String paramKey = storageSourceParam.getKey();
            String paramName = storageSourceParam.getName();

            StorageSourceConfig storageSourceConfig = new StorageSourceConfig();
            storageSourceConfigList.add(storageSourceConfig);

            Object fieldValue = ReflectUtil.getFieldValue(storageSourceAllParam, paramKey);
            String fieldStrValue = Convert.toStr(fieldValue);

            boolean paramRequired = storageSourceParam.isRequired();
            String paramDefaultValue = storageSourceParam.getDefaultValue();

            // 如果是必填的, 并且值为空, 则抛出异常
            if (paramRequired && StrUtil.isEmpty(fieldStrValue)) {
                throw new InitializeStorageSourceException("存储源参数配置错误: [" + paramName + "] 不能为空");
            }

            // 如果默认值不为空, 且输入值为空, 则使用默认值
            if (StrUtil.isNotEmpty(paramDefaultValue) && StrUtil.isEmpty(fieldStrValue)) {
                fieldStrValue = paramDefaultValue;
            }

            storageSourceConfig.setTitle(paramName);
            storageSourceConfig.setName(paramKey);
            storageSourceConfig.setValue(fieldStrValue);
            storageSourceConfig.setType(storageType);
            storageSourceConfig.setStorageId(storageSource.getId());
        }
        storageSourceConfigService.saveBatch(storageSourceConfigList);

        storageSourceContext.init(storageSource.getId());

        AbstractBaseFileService<IStorageParam> driveService = storageSourceContext.get(storageSource.getId());
        if (driveService.getIsUnInitialized()) {
            throw new InitializeStorageSourceException("初始化异常, 请检查配置是否正确.");
        }

        if (storageSource.getAutoRefreshCache()) {
            startAutoCacheRefresh(storageSource.getId());
        } else if (updateFlag) {
            stopAutoCacheRefresh(storageSource.getId());
        }

    }


    /**
     * 查询存储源最大的 ID
     *
     * @return 存储源最大 ID
     */
    public synchronized Integer selectNextId() {
        Integer maxId = storageSourceMapper.selectMaxId();
        if (maxId == null) {
            return 1;
        } else {
            return maxId + 1;
        }
    }



    /**
     * 删除指定存储源设置, 会级联删除其参数设置
     *
     * @param   id
     *          存储源 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        if (log.isDebugEnabled()) {
            log.debug("尝试删除存储源, storageId: {}", id);
        }
        StorageSource storageSource = findById(id);
        storageSourceMapper.deleteById(id);
        storageSourceConfigService.deleteByStorageId(id);
        if (storageSource.getEnableCache()) {
            zFileCache.stopAutoCacheRefresh(id);
            zFileCache.clear(id);
        }
        String key = storageSource.getKey();
        storageIdMapCache.remove(id);
        storageKeyMapCache.remove(key);
        storageSourceContext.destroy(id);
        if (log.isDebugEnabled()) {
            log.debug("尝试删除存储源成功, 已清理相关数据, storageId: {}", id);
        }
    }


    /**
     * 更新指定存储源的缓存启用状态
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   cacheEnable
     *          是否启用缓存
     */
    public void updateCacheStatus(Integer storageId, Boolean cacheEnable) {
        StorageSource storageSource = findById(storageId);
        if (storageSource != null) {
            storageSource.setEnableCache(cacheEnable);
            super.saveOrUpdate(storageSource);
            storageIdMapCache.put(storageId, storageSource);
        }
    }


    /**
     * 获取指定存储源的缓存信息
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  缓存信息
     */
    public CacheInfoDTO findCacheInfo(Integer storageId) {
        long hitCount = zFileCache.getHitCount(storageId);
        long missCount = zFileCache.getMissCount(storageId);
        Set<String> keys = zFileCache.keySet(storageId);
        int cacheCount = keys.size();
        return new CacheInfoDTO(cacheCount, hitCount, missCount, keys);
    }


    /**
     * 刷新指定 key 的缓存:
     * 1. 清空此 key 的缓存.
     * 2. 重新调用方法写入缓存.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   key
     *          缓存 key (文件夹名称)
     */
    public void refreshCache(Integer storageId, String key) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("手动刷新缓存 storageId: {}, key: {}", storageId, key);
        }
        zFileCache.remove(storageId, key);
        AbstractBaseFileService<?> baseFileService = storageSourceContext.get(storageId);
        baseFileService.fileList(key);
    }


    /**
     * 开启缓存自动刷新
     *
     * @param   storageId
     *          存储源 ID
     */
    public void startAutoCacheRefresh(Integer storageId) {
        StorageSource storageSource = findById(storageId);
        storageSource.setAutoRefreshCache(true);
        super.saveOrUpdate(storageSource);
        storageIdMapCache.put(storageId, storageSource);
        zFileCache.startAutoCacheRefresh(storageId);
    }


    /**
     * 停止缓存自动刷新
     *
     * @param   storageId
     *          存储源 ID
     */
    public void stopAutoCacheRefresh(Integer storageId) {
        StorageSource storageSource = findById(storageId);
        storageSource.setAutoRefreshCache(false);
        super.saveOrUpdate(storageSource);
        storageIdMapCache.put(storageId, storageSource);
        zFileCache.stopAutoCacheRefresh(storageId);
    }


    /**
     * 清理缓存
     *
     * @param   storageId
     *          存储源 ID
     */
    public void clearCache(Integer storageId) {
        zFileCache.clear(storageId);
    }


    /**
     * 交换存储源排序
     *
     * @param   updateStorageSortRequestList
     *          更新排序的存储源 id 及排序值列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStorageSort(List<UpdateStorageSortRequest> updateStorageSortRequestList) {
        for (int i = 0; i < updateStorageSortRequestList.size(); i++) {
            UpdateStorageSortRequest item = updateStorageSortRequestList.get(i);
            if (!Objects.equals(i, item.getOrderNum())) {
                storageSourceMapper.updateSetOrderNumById(i, item.getId());
                storageIdMapCache.clear();
            }
        }
    }


    /**
     * 根据存储源 key 获取存储源
     *
     * @param   storageKey
     *          存储源 key
     *
     * @throws  InvalidStorageSourceException   存储源不存在时, 抛出异常.
     *
     * @return  存储源信息
     */
    public StorageSource findByStorageKey(String storageKey) {
        if (storageKeyMapCache.containsKey(storageKey)) {
            return storageKeyMapCache.get(storageKey);
        } else {
            StorageSource storageSource = storageSourceMapper.findByStorageKey(storageKey);
            if (storageSource != null) {
                storageKeyMapCache.put(storageKey, storageSource);
            }
            return storageSource;
        }


    }


    /**
     * 判断存储源 key 是否已存在 (不读取缓存)
     *
     * @param   storageKey
     *          存储源 key
     *
     * @return  是否已存在
     */
    public boolean existByStorageKey(String storageKey) {
        return storageSourceMapper.findIdByStorageKey(storageKey) != null;
    }


    /**
     * 根据存储源 id 获取存储源 key
     *
     * @param   id
     *          存储源 id
     *
     * @return  存储源 key
     */
	public String findKeyById(Integer id){
		 return findById(id).getKey();
	}


    /**
     * 根据存储源 key 获取存储源
     *
     * @param   storageKey
     *          存储源 key
     *
     * @return  存储源信息
     */
    public Integer findIdByKey(String storageKey) {
        StorageSource storageSource = findByStorageKey(storageKey);
        if (storageSource == null) {
            return null;
        } else {
            return storageSource.getId();
        }
    }


    @Override
    public boolean updateById(StorageSource entity) {
        if (entity != null) {
            Integer id = entity.getId();
            storageIdMapCache.put(id, entity);
            storageKeyMapCache.put(entity.getKey(), entity);
        }
        return super.updateById(entity);
    }

}