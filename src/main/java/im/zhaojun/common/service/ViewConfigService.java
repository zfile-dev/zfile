package im.zhaojun.common.service;

import im.zhaojun.common.config.StorageTypeFactory;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.ViewConfig;
import im.zhaojun.common.repository.ViewConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ViewConfigService {

    @Resource
    private ViewConfigRepository viewConfigRepository;

    public ViewConfig getViewConfig() {
        return viewConfigRepository.findFirstBy();
    }

    public FileService getCurrentFileService() {
        ViewConfig viewConfig = getViewConfig();
        StorageTypeEnum storageStrategy = viewConfig.getStorageStrategy();
        return StorageTypeFactory.getStorageTypeService(storageStrategy);
    }

}