package im.zhaojun.zfile.module.storage.context;

import im.zhaojun.zfile.module.storage.model.dto.StorageSourceInitDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.service.StorageSourceConfigService;
import im.zhaojun.zfile.module.storage.service.StorageSourceService;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Slf4j
@Component
@Order(100)
@DependsOn(value = {"storageSourceService"})
public class StorageSourceInitializer implements ApplicationContextAware {

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            Map<String, AbstractBaseFileService> abstractBaseFileServiceMap = applicationContext.getBeansOfType(AbstractBaseFileService.class);
            StorageSourceContext.load(abstractBaseFileServiceMap);
        } catch (Exception e) {
            log.error("初始化存储源 Bean 失败.", e);
            return;
        }

        List<StorageSource> list = storageSourceService.findAllOrderByOrderNum();
        for (StorageSource storageSource : list) {
            try {
                List<StorageSourceConfig> storageSourceConfigList = storageSourceConfigService.selectStorageConfigByStorageId(storageSource.getId());
                StorageSourceInitDTO storageSourceInitDTO = StorageSourceInitDTO.convert(storageSource, storageSourceConfigList);
                StorageSourceContext.init(storageSourceInitDTO);
                log.info("启动时初始化存储源成功, 存储源 id: [{}], 存储源类型: [{}], 存储源名称: [{}]",
                        storageSource.getId(), storageSource.getType().getDescription(), storageSource.getName());
            } catch (Exception e) {
                log.error("启动时初始化存储源失败, 存储源 id: {}, 存储源类型: {}, 存储源名称: {}",
                        storageSource.getId(), storageSource.getType().getDescription(), storageSource.getName(), e);
            }
        }
    }

}
