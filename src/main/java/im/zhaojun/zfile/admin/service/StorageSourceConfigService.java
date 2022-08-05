package im.zhaojun.zfile.admin.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import im.zhaojun.zfile.admin.mapper.StorageSourceConfigMapper;
import im.zhaojun.zfile.admin.model.entity.StorageSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存储源拓展配置 Service
 *
 * @author zhaojun
 */
@Service
@Slf4j
public class StorageSourceConfigService extends ServiceImpl<StorageSourceConfigMapper, StorageSourceConfig> implements IService<StorageSourceConfig> {

    @Resource
    private StorageSourceConfigMapper storageSourceConfigMapper;

    /**
     * 存储源 ID -> 存储源参数列表对象缓存
     */
    private final Map<Integer, List<StorageSourceConfig>> sourceConfigConfigMapCache = new HashMap<>();

    /**
     * 根据存储源 ID 查询存储源拓展配置, 并按照存储源 id 排序
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源拓展配置列表
     */
    public List<StorageSourceConfig> selectStorageConfigByStorageId(Integer storageId) {
        if (sourceConfigConfigMapCache.containsKey(storageId)) {
            return sourceConfigConfigMapCache.get(storageId);
        } else {
            List<StorageSourceConfig> dbResult = storageSourceConfigMapper.findByStorageIdOrderById(storageId);
            sourceConfigConfigMapCache.put(storageId, dbResult);
            return dbResult;
        }
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
        return storageSourceConfigMapper.findByStorageIdAndName(storageId, name);
    }


    /**
     * 批量更新存储源设置
     *
     * @param   storageSourceConfigList
     *          存储源设置列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStorageConfig(List<StorageSourceConfig> storageSourceConfigList) {
        super.updateBatchById(storageSourceConfigList);
        if (CollUtil.isNotEmpty(storageSourceConfigList)) {
            StorageSourceConfig first = CollUtil.getFirst(storageSourceConfigList);
            Integer storageId = first.getStorageId();
            sourceConfigConfigMapCache.remove(storageId);
        }
    }


    /**
     * 根据存储源 id 删除所有设置
     *
     * @param   id
     *          存储源 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByStorageId(Integer id) {
        storageSourceConfigMapper.deleteByStorageId(id);
        sourceConfigConfigMapCache.remove(id);
    }


    /**
     * 批量保存
     *
     * @param   entityList
     *          实体对象集合
     *
     * @return  是否保存成功
     */
    @Override
    public boolean saveBatch(Collection<StorageSourceConfig> entityList) {
        if (CollUtil.isNotEmpty(entityList)) {
            StorageSourceConfig storageSourceConfig = CollUtil.getFirst(entityList);
            Integer storageId = storageSourceConfig.getStorageId();
            sourceConfigConfigMapCache.put(storageId, new ArrayList<>(entityList));
        }
        return saveBatch(entityList, DEFAULT_BATCH_SIZE);
    }

}