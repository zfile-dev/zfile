package im.zhaojun.zfile.common.context;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import im.zhaojun.zfile.admin.annotation.StorageParamItem;
import im.zhaojun.zfile.admin.annotation.model.StorageSourceParamDef;
import im.zhaojun.zfile.admin.model.entity.StorageSource;
import im.zhaojun.zfile.admin.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.admin.model.param.IStorageParam;
import im.zhaojun.zfile.admin.service.StorageSourceConfigService;
import im.zhaojun.zfile.admin.service.StorageSourceService;
import im.zhaojun.zfile.common.config.FlywayDbInitializer;
import im.zhaojun.zfile.common.exception.InvalidStorageSourceException;
import im.zhaojun.zfile.common.util.ClassUtils;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.home.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.home.service.base.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 每个存储源对应一个 Service, 其中初始化好了与对象存储的配置信息.
 * 此存储源上下文环境用户缓存每个 Service, 避免重复初始化.
 *
 * 依赖 {@link FlywayDbInitializer} 初始化数据库后执行.
 *
 * @author zhaojun
 */
@Component
@DependsOn("flywayDbInitializer")
@Slf4j
public class StorageSourceContext implements ApplicationContextAware {

    /**
     * Map<Integer, AbstractBaseFileService>
     * Map<存储源 ID, 存储源 Service>
     */
    private static final Map<Integer, AbstractBaseFileService> DRIVES_SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存每个存储源参数的字段列表.
     */
    Map<Class<?>, Map<String, Field>> PARAM_CLASS_FIELD_NAME_MAP_CACHE = new HashMap<>();

    /**
     * Map<存储源类型, 存储源 Service>
     */
    private static Map<String, AbstractBaseFileService> storageTypeEnumFileServiceMap;

    @Resource
    private StorageSourceService storageSourceService;

    @Resource
    private StorageSourceConfigService storageSourceConfigService;


    /**
     * 项目启动时, 自动调用数据库已存储的所有存储源进行初始化.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        storageTypeEnumFileServiceMap = applicationContext.getBeansOfType(AbstractBaseFileService.class);

        List<StorageSource> list = storageSourceService.findAllOrderByOrderNum();
        for (StorageSource storageSource : list) {
            try {
                init(storageSource.getId());
                log.info("启动时初始化存储源成功, 存储源信息: {}", JSON.toJSONString(storageSource));
            } catch (Exception e) {
                log.error("启动时初始化存储源失败, 存储源信息: {}", JSON.toJSONString(storageSource), e);
            }
        }
    }


    /**
     * 初始化指定存储源的 Service, 添加到上下文环境中.
     *
     * @param   storageId
     *          存储源 ID.
     */
    public void init(Integer storageId) {
        AbstractBaseFileService<IStorageParam> baseFileService = getInitStorageBeanByStorageId(storageId);
        if (baseFileService != null) {
            if (log.isDebugEnabled()) {
                log.debug("尝试初始化存储源, storageId: {}", storageId);
            }

            baseFileService.setStorageId(storageId);
            IStorageParam initParam = getInitParam(storageId, baseFileService);
            baseFileService.setParam(initParam);

            baseFileService.init();

            baseFileService.testConnection();
            if (log.isDebugEnabled()) {
                log.debug("初始化存储源成功, storageId: {}", storageId);
            }
            DRIVES_SERVICE_MAP.put(storageId, baseFileService);
        }
    }

    /**
     * 获取指定存储源的初始化参数.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源初始化参数
     */
    private IStorageParam getInitParam(Integer storageId, AbstractBaseFileService<?> baseFileService) {
        List<StorageSourceConfig> storageSourceConfigList = storageSourceConfigService.selectStorageConfigByStorageId(storageId);

        // 获取存储源实现类的实际 Class
        Class<?> beanTargetClass = AopUtils.getTargetClass(baseFileService);
        // 获取存储源实现类的实际 Class 的泛型参数类型
        Class<?> paramClass = ClassUtils.getClassFirstGenericsParam(beanTargetClass);
        String paramClassName = paramClass.getName();

        IStorageParam iStorageParam = ReflectUtil.newInstance(paramClassName);


        // 获取存储器参数 key -> 存储器 field 对照关系，如果缓存中有，则从缓存中取.
        Map<String, Field> fieldMap = new HashMap<>();
        if (PARAM_CLASS_FIELD_NAME_MAP_CACHE.containsKey(paramClass)) {
            fieldMap = PARAM_CLASS_FIELD_NAME_MAP_CACHE.get(paramClass);
        } else {
            Field[] fields = ReflectUtil.getFieldsDirectly(paramClass, true);
            for (Field field : fields) {
                String key;

                StorageParamItem storageParamItem = field.getDeclaredAnnotation(StorageParamItem.class);
                // 没有注解或注解中没有配置 key 则使用字段名.
                if (storageParamItem == null || StrUtil.isEmpty(storageParamItem.key())) {
                    key = field.getName();
                } else {
                    key = storageParamItem.key();
                }

                // 如果 map 中包含此 key, 则是父类的, 跳过.
                if (fieldMap.containsKey(key)) {
                    continue;
                }

                fieldMap.put(key, field);
            }
            PARAM_CLASS_FIELD_NAME_MAP_CACHE.put(paramClass, fieldMap);
        }

        for (StorageSourceConfig storageSourceConfig : storageSourceConfigList) {
            String name = storageSourceConfig.getName();
            String value = storageSourceConfig.getValue();
            try {
                Field field = fieldMap.get(name);
                ReflectUtil.setFieldValue(iStorageParam, field, value);
            } catch (Exception e) {
                log.error("初始化存储源参数失败, storageId: {}, name: {}, value: {}", storageId, name, value, e);
            }
        }

        return iStorageParam;
    }


    /**
     * 根据存储源 id 获取对应的 Service.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源对应的 Service
     */
    public AbstractBaseFileService<IStorageParam> get(Integer storageId) {
        AbstractBaseFileService<IStorageParam> abstractBaseFileService = DRIVES_SERVICE_MAP.get(storageId);
        if (abstractBaseFileService == null) {
            throw new InvalidStorageSourceException("无效的存储源, storageId: " + storageId);
        }
        return abstractBaseFileService;
    }


    /**
     * 根据存储源 key 获取对应的 Service.
     *
     * @param   key
     *          存储源 key
     *
     * @return  存储源对应的 Service
     */
    public AbstractBaseFileService<?> getByKey(String key) {
        return get(storageSourceService.findIdByKey(key));
    }


    /**
     * 销毁指定存储源的 Service.
     *
     * @param   storageId
     *          存储源 ID
     */
    public void destroy(Integer storageId) {
        if (log.isDebugEnabled()) {
            log.debug("清理存储源上下文对象, storageId: {}", storageId);
        }
        DRIVES_SERVICE_MAP.remove(storageId);
    }


    /**
     * 根据存储类型获取对应的存储源的参数列表.
     *
     * @param   type
     *          存储类型: {@link StorageTypeEnum}
     *
     * @return  指定类型存储源的参数列表. {@link AbstractBaseFileService#getStorageSourceParamList()}
     */
    public static List<StorageSourceParamDef> getStorageSourceParamListByType(StorageTypeEnum type) {
        AbstractBaseFileService<?> service = null;
        for (AbstractBaseFileService<?> fileService : storageTypeEnumFileServiceMap.values()) {
            if (fileService.getStorageTypeEnum() == type) {
                service = fileService;
                break;
            }
        }
        if (service != null) {
            return service.getStorageSourceParamList();
        }
        return Collections.emptyList();
    }


    /**
     * 获取指定存储源初始状态的 Service.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源对应未初始化的 Service
     */
    private AbstractBaseFileService getInitStorageBeanByStorageId(Integer storageId) {
        StorageTypeEnum storageTypeEnum = storageSourceService.findStorageTypeById(storageId);
        for (AbstractBaseFileService<?> value : storageTypeEnumFileServiceMap.values()) {
            if (Objects.equals(value.getStorageTypeEnum(), storageTypeEnum)) {
                return SpringUtil.getBean(value.getClass());
            }
        }
        return null;
    }


    /**
     * 获取所有 AccessToken 机制的存储源, 这些存储源都继承类 {@link RefreshTokenService}.
     *
     * @return  获取所有需要刷新 AccessToken 的存储源.
     */
    public Map<Integer, RefreshTokenService> getAllRefreshTokenStorageSource() {
        Map<Integer, RefreshTokenService> result = new HashMap<>();

        for (Map.Entry<Integer, AbstractBaseFileService> baseFileServiceEntry : DRIVES_SERVICE_MAP.entrySet()) {
            Integer storageId = baseFileServiceEntry.getKey();
            AbstractBaseFileService<?> baseFileService = baseFileServiceEntry.getValue();
            // 如果未初始化成功, 则直接跳过
            if (baseFileService.getIsUnInitialized()) {
                continue;
            }

            if (baseFileService instanceof RefreshTokenService) {
                result.put(storageId, (RefreshTokenService) baseFileService);
            }
        }

        return result;
    }

}