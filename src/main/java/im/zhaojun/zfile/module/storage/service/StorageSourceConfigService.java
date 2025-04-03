package im.zhaojun.zfile.module.storage.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import im.zhaojun.zfile.core.exception.biz.InitializeStorageSourceBizException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.context.StorageSourceContext;
import im.zhaojun.zfile.module.storage.event.StorageSourceCopyEvent;
import im.zhaojun.zfile.module.storage.event.StorageSourceDeleteEvent;
import im.zhaojun.zfile.module.storage.mapper.StorageSourceConfigMapper;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceAllParamDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 根据存储源 ID 查询存储源拓展配置, 并按照存储源 id 排序
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源拓展配置列表
     */
    @Cacheable(key = "#storageId", unless = "#result == null or #result.size() == 0", condition = "#storageId != null ")
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
        return ((StorageSourceConfigService) AopContext.currentProxy())
                .selectStorageConfigByStorageId(storageId)
                .stream()
                .filter(storageSourceConfig -> StringUtils.equals(name, storageSourceConfig.getName()))
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
     * 监听存储源删除事件，根据存储源 id 删除相关的存储源参数
     *
     * @param   storageSourceDeleteEvent
     *          存储源删除事件
     */
    @EventListener
    public void onStorageSourceDelete(StorageSourceDeleteEvent storageSourceDeleteEvent) {
        Integer storageId = storageSourceDeleteEvent.getId();
        int updateRows = ((StorageSourceConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);
        if (log.isDebugEnabled()) {
            log.debug("删除存储源 [id {}, name: {}, type: {}] 时，关联删除存储源参数 {} 条",
                    storageId,
                    storageSourceDeleteEvent.getName(),
                    storageSourceDeleteEvent.getType().getDescription(),
                    updateRows);
        }
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
        ((StorageSourceConfigService) AopContext.currentProxy()).deleteByStorageId(storageId);

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
    public List<StorageSourceConfig> storageSourceAllParamToConfigList(Integer storageId, StorageTypeEnum storageType, StorageSourceAllParamDTO storageSourceAllParam) {
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
            if (paramRequired && StringUtils.isEmpty(fieldStrValue)) {
                String errMsg = String.format("参数「%s」不能为空", storageSourceParam.getName());
                throw new InitializeStorageSourceBizException(errMsg, storageId);
            }

            // 校验如果有默认值，则填充默认值
            String paramDefaultValue = storageSourceParam.getDefaultValue();
            if (StringUtils.isNotEmpty(paramDefaultValue) && StringUtils.isEmpty(fieldStrValue)) {
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

    /**
     * 监听存储源复制事件, 复制存储源时, 复制存储源参数配置
     *
     * @param   storageSourceCopyEvent
     *          存储源复制事件
     */
    @EventListener
    public void onStorageSourceCopy(StorageSourceCopyEvent storageSourceCopyEvent) {
        Integer fromId = storageSourceCopyEvent.getFromId();
        Integer newId = storageSourceCopyEvent.getNewId();

        List<StorageSourceConfig> storageSourceConfigList = ((StorageSourceConfigService) AopContext.currentProxy())
                .selectStorageConfigByStorageId(fromId);

        storageSourceConfigList.forEach(storageSourceConfig -> {
            StorageSourceConfig newStorageSourceConfig = new StorageSourceConfig();
            BeanUtils.copyProperties(storageSourceConfig, newStorageSourceConfig);
            newStorageSourceConfig.setId(null);
            newStorageSourceConfig.setStorageId(newId);
            storageSourceConfigMapper.insert(newStorageSourceConfig);
        });

        log.info("复制存储源 ID 为 {} 的存储源参数配置到存储源 ID 为 {} 成功, 共 {} 条", fromId, newId, storageSourceConfigList.size());
    }

}