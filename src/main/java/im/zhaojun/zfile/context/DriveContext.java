package im.zhaojun.zfile.context;

import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.DriveConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.SpringContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 驱动器上下文环境
 * @author zhaojun
 */
@Component
@DependsOn("springContextHolder")
public class DriveContext implements ApplicationContextAware {

    private static Map<Integer, AbstractBaseFileService> drivesServiceMap = new ConcurrentHashMap<>();

    private static Map<StorageTypeEnum, Class<AbstractBaseFileService>> storageTypeEnumClassMap = new ConcurrentHashMap<>();

    @Resource
    private DriveConfigService driveConfigService;


    /**
     * 初始化指定驱动器的 Service, 添加到上下文环境中.
     *
     * @param   driveId
     *          驱动器 ID.
     */
    public void initDrive(Integer driveId) {
        AbstractBaseFileService baseFileService = getBeanByDriveId(driveId);
        if (baseFileService != null) {
            baseFileService.init(driveId);
            drivesServiceMap.put(driveId, baseFileService);
        }
    }


    /**
     * 获取指定驱动器的 Service.
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  驱动器对应的 Service
     */
    public AbstractBaseFileService getDriveService(Integer driveId) {
        return drivesServiceMap.get(driveId);
    }


    /**
     * 销毁指定驱动器的 Service.
     *
     * @param   driveId
     *          驱动器 ID
     */
    public void destroyDrive(Integer driveId) {
        drivesServiceMap.remove(driveId);
    }


    /**
     * 获取指定驱动器对应的 Service, 状态为未初始化
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  驱动器对应未初始化的 Service
     */
    private AbstractBaseFileService getBeanByDriveId(Integer driveId) {
        StorageTypeEnum storageTypeEnum = driveConfigService.findStorageTypeById(driveId);
        Map<String, AbstractBaseFileService> beansOfType = SpringContextHolder.getBeansOfType(AbstractBaseFileService.class);
        for (AbstractBaseFileService value : beansOfType.values()) {
            if (Objects.equals(value.getStorageTypeEnum(), storageTypeEnum)) {
                return SpringContextHolder.getBean(value.getClass());
            }
        }
        return null;
    }


    /**
     * 项目启动时, 自动调用所有驱动器进行初始化.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<DriveConfig> list = driveConfigService.list();
        for (DriveConfig driveConfig : list) {
            initDrive(driveConfig.getId());
        }
    }

}