package im.zhaojun.common.service;

import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.repository.StorageConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorageConfigService {

    @Resource
    private StorageConfigRepository storageConfigRepository;

    private List<StorageConfig> selectStorageConfigByType(StorageTypeEnum storageTypeEnum) {
        return storageConfigRepository.findByType(storageTypeEnum);
    }

    public Map<String, StorageConfig> selectStorageConfigMapByKey(StorageTypeEnum storageTypeEnum) {
        Map<String, StorageConfig> map = new HashMap<>();
        for (StorageConfig storageConfig : selectStorageConfigByType(storageTypeEnum)) {
            map.put(storageConfig.getKey(), storageConfig);
        }
        return map;
    }
}
