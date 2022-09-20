package im.zhaojun.zfile.module.storage.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.module.storage.mapper.StorageSourceConfigMapper;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.core.exception.file.init.InitializeStorageSourceException;
import im.zhaojun.zfile.core.util.CodeMsg;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceAllParamDTO;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 存储源拓展配置 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "storageSourceConfig")
public class StorageSourceConfigService {

    @Resource
    private StorageSourceConfigMapper storageSourceConfigMapper;
    
    @Resource
    private StorageSourceConfigService storageSourceConfigService;
    
    /**
     * 根据存储源 ID 查询存储源拓展配置, 并按照存储源 id 排序
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源拓展配置列表
     */
    @Cacheable(key = "#storageId", unless = "#result == null or #result.size() == 0")
    public List<StorageSourceConfig> selectStorageConfigByStorageId(Integer storageId) {
        return storageSourceConfigMapper.findByStorageIdOrderById(storageId);
    }


    /**
     * 获取指定存储源的指定参数名称
     *
     * @param   storageId
     *          存储源 id
     *
     * @param   name
     *          参数名
     *
     * @return  参数信息
     */
    public StorageSourceConfig findByStorageIdAndName(Integer storageId, String name) {
        return storageSourceConfigService
                .selectStorageConfigByStorageId(storageId)
                .stream()
                .filter(storageSourceConfig -> StrUtil.equals(name, storageSourceConfig.getName()))
                .findFirst()
                .orElse(null);
    }

    
    /**
     * 根据存储源 id 删除所有设置
     *
     * @param   storageId
     *          存储源 ID
     */
    @CacheEvict(key = "#storageId", beforeInvocation = true)
    public int deleteByStorageId(Integer storageId) {
        int deleteSize = storageSourceConfigMapper.deleteByStorageId(storageId);
        log.info("删除存储源 ID 为 {} 的参数配置 {} 条", storageId, deleteSize);
        return deleteSize;
    }


    /**
     * 批量保存
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   configList
     *          实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(Integer storageId, Collection<StorageSourceConfig> configList) {
        storageSourceConfigService.deleteByStorageId(storageId);
    
        log.info("更新存储源 ID 为 {} 的参数配置 {} 条", storageId, configList.size());
    
        configList.forEach(storageSourceConfig -> {
            storageSourceConfig.setStorageId(storageId);
            storageSourceConfigMapper.insert(storageSourceConfig);
    
            if (log.isDebugEnabled()) {
                log.debug("新增存储源参数配置, 存储源 ID: {}, 存储源类型: {}, 参数名: {}",
                        storageSourceConfig.getStorageId(), storageSourceConfig.getType().getDescription(),
                        storageSourceConfig.getName());
            }
        });
    }
    
    /**
     * 批量更新存储源设置
     *
     * @param   storageSourceConfigList
     *          存储源设置列表
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#storageId")
    public void updateBatch(Integer storageId, List<StorageSourceConfig> storageSourceConfigList) {
        storageSourceConfigList.forEach(storageSourceConfig -> {
            storageSourceConfig.setStorageId(storageId);
            storageSourceConfigMapper.updateById(storageSourceConfig);
    
            if (log.isDebugEnabled()) {
                log.debug("更新存储源参数配置, 存储源 ID: {}, 存储源类型: {}, 参数名: {}",
                        storageSourceConfig.getStorageId(), storageSourceConfig.getType().getDescription(),
                        storageSourceConfig.getName());
            }
        });
    }
    
    
    /**
     * 将存储源所有参数转换成指定存储类型的参数对象列表
     *
     * @param   storageId
     *          存储源 ID
     *
     * @param   storageType
     *          存储源类型
     *
     * @param   storageSourceAllParam
     *          存储源所有参数
     */
    public List<StorageSourceConfig> toStorageSourceConfigList(Integer storageId, StorageTypeEnum storageType, StorageSourceAllParamDTO storageSourceAllParam) {
        // 返回结果
        List<StorageSourceConfig> result = new ArrayList<>();
        
        // 获取该存储源类型需要的参数列表
        List<StorageSourceParamDef> storageSourceParamList = StorageSourceContext.getStorageSourceParamListByType(storageType);
        
        // 遍历参数列表, 将参数转换成存储源参数对象
        for (StorageSourceParamDef storageSourceParam : storageSourceParamList) {
            // 根据字段名称获取字段值
            Object fieldValue = ReflectUtil.getFieldValue(storageSourceAllParam, storageSourceParam.getKey());
            String fieldStrValue = Convert.toStr(fieldValue);
            
            // 校验是否必填, 如果不符合则抛出异常
            boolean paramRequired = storageSourceParam.isRequired();
            if (paramRequired && StrUtil.isEmpty(fieldStrValue)) {
                String errMsg = StrUtil.format("参数「{}」不能为空", storageSourceParam.getName());
                throw new InitializeStorageSourceException(CodeMsg.STORAGE_SOURCE_INIT_STORAGE_CONFIG_FAIL,
                        storageId, errMsg).setResponseExceptionMessage(true);
            }
            
            // 校验如果有默认值，则填充默认值
            String paramDefaultValue = storageSourceParam.getDefaultValue();
            if (StrUtil.isNotEmpty(paramDefaultValue) && StrUtil.isEmpty(fieldStrValue)) {
                fieldStrValue = paramDefaultValue;
            }
            
            // 添加到结果列表
            StorageSourceConfig storageSourceConfig = new StorageSourceConfig();
            storageSourceConfig.setTitle(storageSourceParam.getName());
            storageSourceConfig.setName(storageSourceParam.getKey());
            storageSourceConfig.setValue(fieldStrValue);
            storageSourceConfig.setType(storageType);
            storageSourceConfig.setStorageId(storageId);
            result.add(storageSourceConfig);
        }
        
        return result;
    }
    
}