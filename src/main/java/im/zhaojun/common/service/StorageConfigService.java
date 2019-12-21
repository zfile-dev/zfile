package im.zhaojun.common.service;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.repository.StorageConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
public class StorageConfigService {

    @Resource
    private StorageConfigRepository storageConfigRepository;

    public List<StorageConfig> selectStorageConfigByType(StorageTypeEnum storageTypeEnum) {
        return storageConfigRepository.findByTypeOrderById(storageTypeEnum);
    }

    public Map<String, StorageConfig> selectStorageConfigMapByKey(StorageTypeEnum storageTypeEnum) {
        Map<String, StorageConfig> map = new HashMap<>(24);
        for (StorageConfig storageConfig : selectStorageConfigByType(storageTypeEnum)) {
            map.put(storageConfig.getKey(), storageConfig);
        }
        return map;
    }

    public void updateStorageConfig(List<StorageConfig> storageConfigList) {
        storageConfigRepository.saveAll(storageConfigList);
    }

}
