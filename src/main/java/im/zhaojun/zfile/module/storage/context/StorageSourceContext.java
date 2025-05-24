package im.zhaojun.zfile.module.storage.context;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.core.exception.biz.InvalidStorageSourceBizException;
import im.zhaojun.zfile.core.util.ClassUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.module.storage.model.dto.StorageSourceInitDTO;
import im.zhaojun.zfile.module.storage.model.entity.StorageSource;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import im.zhaojun.zfile.module.storage.support.StorageSourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 每个存储源对应一个 Service, 其中初始化好了与对象存储的配置信息.
 * 此存储源上下文环境用户缓存每个 Service, 避免重复初始化.
 * <br>
 *
 * @author zhaojun
 */
@Slf4j
public class StorageSourceContext {

    /**
     * Map<Integer, AbstractBaseFileService>
     * Map<存储源 ID, 存储源 Service>
     */
    private static final Map<Integer, AbstractBaseFileService<IStorageParam>> DRIVES_SERVICE_MAP = new ConcurrentHashMap<>();


    /**
     * Map<String, Integer>
     * Map<存储源 Key, 存储源 ID>
     */
    private static final Map<String, Integer> STORAGE_KEY_ID_MAP = new ConcurrentHashMap<>();


    /**
     * Map<存储源类型的bean名称, 存储源 Service>
     */
    private static Map<String, AbstractBaseFileService> storageTypeServiceNameMap;

    /**
     * Map<存储源枚举类型, 存储源 Service>
     */
    private static Map<StorageTypeEnum, AbstractBaseFileService> storageTypeEnumFileServiceMap = new HashMap<>();

    /**
     * 缓存每个存储源参数的字段列表.
     */
    private static final Map<Class<?>, Map<String, Field>> PARAM_CLASS_FIELD_NAME_MAP_CACHE = new HashMap<>();

    /**
     * 项目启动时, 自动调用数据库已存储的所有存储源进行初始化.
     */
    static void load(Map<String, AbstractBaseFileService> storageTypeServiceNameMap) {
        StorageSourceContext.storageTypeServiceNameMap = storageTypeServiceNameMap;
        for (AbstractBaseFileService value : storageTypeServiceNameMap.values()) {
            storageTypeEnumFileServiceMap.put(value.getStorageTypeEnum(), value);
        }
    }


    /**
     * 根据存储源 id 获取对应的 Service.
     *
     * @param   storageId
     *          存储源 ID
     *
     * @return  存储源对应的 Service
     */
    public static AbstractBaseFileService<IStorageParam> getByStorageId(Integer storageId) {
        AbstractBaseFileService<IStorageParam> abstractBaseFileService = DRIVES_SERVICE_MAP.get(storageId);
        if (abstractBaseFileService == null) {
            throw new InvalidStorageSourceBizException(storageId);
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
    public static AbstractBaseFileService<?> getByStorageKey(String key) {
        Integer storageId = STORAGE_KEY_ID_MAP.get(key);
        if (storageId == null) {
            return null;
        }
        return getByStorageId(storageId);
    }


    /**
     * 根据存储源类型获取对应的 Service.
     *
     * @param   storageTypeEnum
     *          存储源类型(枚举)
     *
     * @return  存储源对应的 Service
     */
    public static AbstractBaseFileService<?> getByStorageTypeEnum(StorageTypeEnum storageTypeEnum) {
        return storageTypeEnumFileServiceMap.get(storageTypeEnum);
    }


    /**
     * 根据存储类型获取对应的存储源的参数列表.
     *
     * @param   type
     *          存储类型: {@link StorageTypeEnum}
     *
     * @return  指定类型存储源的参数列表. {@link StorageSourceSupport#getStorageSourceParamList(AbstractBaseFileService)} )}}
     */
    public static List<StorageSourceParamDef> getStorageSourceParamListByType(StorageTypeEnum type) {
        return storageTypeServiceNameMap.values().stream()
                // 根据存储源类型找到第一个匹配的 Service
                .filter(fileService -> fileService.getStorageTypeEnum() == type)
                .findFirst()
                // 获取该 Service 的参数列表
                .map(StorageSourceSupport::getStorageSourceParamList)
                // 如果没有找到, 则返回空列表
                .orElse(Collections.emptyList());
    }


    /**
     * 初始化指定存储源的 Service, 添加到上下文环境中.
     *
     * @param   storageSourceInitDTO
     *          存储源初始化对象
     */
    public static void init(StorageSourceInitDTO storageSourceInitDTO) {
        Integer storageId = storageSourceInitDTO.getId();
        String storageName = storageSourceInitDTO.getName();
        String key = storageSourceInitDTO.getKey();
        StorageTypeEnum storageTypeEnum = storageSourceInitDTO.getType();

        AbstractBaseFileService<IStorageParam> baseFileService = getInitStorageBeanByStorageType(storageTypeEnum);
        if (baseFileService == null) {
            throw new InvalidStorageSourceBizException(storageId);
        }

        // 填充初始化参数
        IStorageParam initParam = getInitParam(baseFileService, storageSourceInitDTO.getStorageSourceConfigList());

        // 进行初始化并测试连接
        baseFileService.init(storageName, storageId, initParam);
        baseFileService.testConnection();

        DRIVES_SERVICE_MAP.put(storageId, baseFileService);
        STORAGE_KEY_ID_MAP.put(key, storageId);
    }


    /**
     * 获取指定存储源初始状态的 Service.
     *
     * @param   storageTypeEnum
     *          存储源类型
     *
     * @return  存储源对应未初始化的 Service
     */
    private static AbstractBaseFileService<IStorageParam> getInitStorageBeanByStorageType(StorageTypeEnum storageTypeEnum) {
        for (AbstractBaseFileService<?> value : storageTypeServiceNameMap.values()) {
            if (Objects.equals(value.getStorageTypeEnum(), storageTypeEnum)) {
                return SpringUtil.getBean(value.getClass());
            }
        }
        return null;
    }


    /**
     * 获取指定存储源的初始化参数.
     */
    private static IStorageParam getInitParam(AbstractBaseFileService<?> baseFileService, List<StorageSourceConfig> storageSourceConfigList) {
        // 获取存储源实现类的实际 Class
        Class<?> beanTargetClass = AopUtils.getTargetClass(baseFileService);
        // 获取存储源实现类的实际 Class 的泛型参数类型
        Class<?> paramClass = ClassUtils.getClassFirstGenericsParam(beanTargetClass);

        // 获取存储器参数 key -> 存储器 field 对照关系，如果缓存中有，则从缓存中取.
        Map<String, Field> fieldMap = new HashMap<>();
        if (PARAM_CLASS_FIELD_NAME_MAP_CACHE.containsKey(paramClass)) {
            fieldMap = PARAM_CLASS_FIELD_NAME_MAP_CACHE.get(paramClass);
        } else {
            Field[] fields = ReflectUtil.getFieldsDirectly(paramClass, true);
            List<String> ignoreFieldNameList = new ArrayList<>();
            for (Field field : fields) {
                String key;

                StorageParamItem storageParamItem = field.getDeclaredAnnotation(StorageParamItem.class);
                // 没有注解或注解中没有配置 key 则使用字段名.
                if (storageParamItem == null || StringUtils.isEmpty(storageParamItem.key())) {
                    key = field.getName();
                } else {
                    key = storageParamItem.key();
                }

                if (storageParamItem != null && storageParamItem.ignoreInput()) {
                    ignoreFieldNameList.add(key);
                }

                // 如果 map 中包含此 key, 则是父类的, 跳过.
                if (fieldMap.containsKey(key)) {
                    continue;
                }

                if (!ignoreFieldNameList.contains(key)) {
                    fieldMap.put(key, field);
                }
            }
            PARAM_CLASS_FIELD_NAME_MAP_CACHE.put(paramClass, fieldMap);
        }

        // 实例化参数对象
        IStorageParam iStorageParam = ReflectUtil.newInstance(paramClass.getName());

        Map<String, Field> fieldMapCopy = new HashMap<>(fieldMap);

        // 给所有字段填充值
        for (StorageSourceConfig storageSourceConfig : storageSourceConfigList) {
            String name = storageSourceConfig.getName();
            String value = storageSourceConfig.getValue();
            try {
                Field field = fieldMap.get(name);
                ReflectUtil.setFieldValue(iStorageParam, field, value);
                fieldMapCopy.remove(name);
            } catch (Exception e) {
                log.warn("存储源 {} 从数据库获取存储源参数进行初始化时为字段 {} 初始化值 {} 失败", baseFileService, name, value, e);
            }
        }

        if (!fieldMapCopy.isEmpty()) {
            List<StorageSourceParamDef> storageSourceParamList = StorageSourceSupport.getStorageSourceParamList(baseFileService);
            Map<String, StorageSourceParamDef> storageSourceParamDefMap = storageSourceParamList.stream()
                    .collect(Collectors.toMap(StorageSourceParamDef::getKey, Function.identity()));

            // 如果还有字段没有填充值, 则使用默认值填充.
            for (Map.Entry<String, Field> entry : fieldMapCopy.entrySet()) {
                Field field = entry.getValue();
                StorageSourceParamDef storageSourceParamDef = storageSourceParamDefMap.get(entry.getKey());
                if (storageSourceParamDef == null) {
                    continue;
                }

                String defaultValue = storageSourceParamDef.getDefaultValue();
                if (StringUtils.isBlank(defaultValue)) {
                    continue;
                }
                ReflectUtil.setFieldValue(iStorageParam, field, defaultValue);
                if (log.isDebugEnabled()) {
                    log.debug("存储源 {} 数据库未设置字段 {} 值，使用默认值 {}", baseFileService, entry.getKey(), defaultValue);
                }
            }
        }

        return iStorageParam;
    }


    /**
     * 获取所有 AccessToken 机制的存储源, 这些存储源都继承类 {@link RefreshTokenService}.
     *
     * @return  获取所有需要刷新 AccessToken 的存储源.
     */
    public static Map<Integer, RefreshTokenService> getAllRefreshTokenStorageSource() {
        Map<Integer, RefreshTokenService> result = new HashMap<>();

        for (Map.Entry<Integer, AbstractBaseFileService<IStorageParam>> baseFileServiceEntry : DRIVES_SERVICE_MAP.entrySet()) {
            Integer storageId = baseFileServiceEntry.getKey();
            AbstractBaseFileService<?> baseFileService = baseFileServiceEntry.getValue();
            // 如果未初始化成功, 则直接跳过
            if (BooleanUtils.isNotTrue(baseFileService.isInitialized())) {
                continue;
            }

            if (baseFileService instanceof RefreshTokenService) {
                result.put(storageId, (RefreshTokenService) baseFileService);
            }
        }

        return result;
    }


    /**
     * 销毁指定存储源的 Service.
     *
     * @param   storageSource
     *          存储源类
     */
    public static void destroy(StorageSource storageSource) {
        Integer id = storageSource.getId();
        String key = storageSource.getKey();
        log.info("清理存储源上下文对象, storageId: {}, storageKey: {}", id, key);
        AbstractBaseFileService<IStorageParam> abstractBaseFileService = DRIVES_SERVICE_MAP.remove(id);
        if (abstractBaseFileService != null) {
            abstractBaseFileService.destroy();
        }

        STORAGE_KEY_ID_MAP.remove(key);
    }


}
