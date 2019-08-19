package im.zhaojun.common.service;

import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.mapper.StorageConfigMapper;
import im.zhaojun.common.model.StorageConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorageConfigService {

    @Resource
    private StorageConfigMapper storageConfigMapper;

    public List<StorageConfig> selectStorageConfigByType(StorageTypeEnum storageTypeEnum) {
        return storageConfigMapper.selectStorageConfigByType(storageTypeEnum);
    }

    public Map<String, StorageConfig> selectStorageConfigMapByKey(StorageTypeEnum storageTypeEnum) {
        Map<String, StorageConfig> map = new HashMap<>();
        for (StorageConfig storageConfig : selectStorageConfigByType(storageTypeEnum)) {
            map.put(storageConfig.getKey(), storageConfig);
        }
        return map;
    }
}
