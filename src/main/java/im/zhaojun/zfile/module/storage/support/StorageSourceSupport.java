package im.zhaojun.zfile.module.storage.support;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.core.util.*;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelect;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储源支持类
 *
 * @author zhaojun
 */
public class StorageSourceSupport {

    /**
     * 存储源类型与存储源参数列表的缓存
     */
    private static final Map<Class<? extends AbstractBaseFileService>, List<StorageSourceParamDef>> STORAGE_SOURCE_PARAM_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取指定存储源所有的参数列表定义
     *
     * @return 存储源参数列表定义
     */
    public static List<StorageSourceParamDef> getStorageSourceParamList(AbstractBaseFileService abstractBaseFileService) {
        Class<? extends AbstractBaseFileService> clazz;
        if (AopUtils.isAopProxy(abstractBaseFileService)) {
            clazz = (Class<? extends AbstractBaseFileService>) AopUtils.getTargetClass(abstractBaseFileService);
        } else {
            clazz = abstractBaseFileService.getClass();
        }
        IStorageParam storageParam = abstractBaseFileService.getParam();
        // 如果缓存中有, 则直接返回
        if (STORAGE_SOURCE_PARAM_CACHE.containsKey(clazz)) {
            return STORAGE_SOURCE_PARAM_CACHE.get(clazz);
        }

        Map<String, StorageSourceParamDef> storageSourceParamDefMap = new HashMap<>();

        // 获取存储源实现类的泛型参数类型
        Class<?> paramClass = ClassUtils.getClassFirstGenericsParam(clazz);
        Field[] fields = ReflectUtil.getFields(paramClass);

        // 已添加的字段列表.
        List<String> useFieldNames = new ArrayList<>();

        Map<String, Set<StorageParamItemAnnoEnum>> fieldOverrideMap = new HashMap<>();

        for (Field field : fields) {
            // 获取字段上的注解
            StorageParamItem storageParamItemAnnotation = field.getAnnotation(StorageParamItem.class);
            if (storageParamItemAnnotation == null) {
                continue;
            }

            // 如果字段被忽略, 则添加到忽略列表中
            String fieldName = field.getName();
            if (storageParamItemAnnotation.ignoreInput()) {
                useFieldNames.add(fieldName);
                continue;
            }

            String key = storageParamItemAnnotation.key();
            String name = storageParamItemAnnotation.name();
            String description = storageParamItemAnnotation.description();
            boolean required = storageParamItemAnnotation.required();
            String defaultValue = PlaceholderUtils.resolvePlaceholdersBySpringProperties(storageParamItemAnnotation.defaultValue());
            String link = parseAnnotationLinkField(storageParamItemAnnotation);
            String linkName = storageParamItemAnnotation.linkName();
            StorageParamTypeEnum type = storageParamItemAnnotation.type();
            List<StorageSourceParamDef.Options> optionsList = getOptionsList(storageParamItemAnnotation, storageParam);
            boolean optionAllowCreate = storageParamItemAnnotation.optionAllowCreate();
            int order = storageParamItemAnnotation.order();
            boolean pro = storageParamItemAnnotation.pro();
            String condition = storageParamItemAnnotation.condition();
            boolean hidden = storageParamItemAnnotation.hidden();

            // 默认 key 为字段名，默认 name 为 key
            if (StringUtils.isEmpty(key)) key = fieldName;
            if (StringUtils.isEmpty(name)) name = key;

            // 如果字段已存在且不是覆盖属性, 则跳过
            if (useFieldNames.contains(fieldName) && !fieldOverrideMap.containsKey(fieldName)) {
                continue;
            }

            Set<StorageParamItemAnnoEnum> fieldOverrideSet = fieldOverrideMap.get(fieldName);

            // 如果默认值不为空, 则该字段则不是必填的
            if (StringUtils.isNotEmpty(defaultValue)) {
                required = false;
            }

            StorageSourceParamDef storageSourceParamDef = storageSourceParamDefMap.getOrDefault(fieldName, new StorageSourceParamDef());
            boolean fieldOverrideSetIsEmpty = CollectionUtils.isEmpty(fieldOverrideSet);
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.KEY)) {
                storageSourceParamDef.setKey(key);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.NAME)) {
                storageSourceParamDef.setName(name);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.DESCRIPTION)) {
                storageSourceParamDef.setDescription(description);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.REQUIRED)) {
                storageSourceParamDef.setRequired(required);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.DEFAULT_VALUE)) {
                storageSourceParamDef.setDefaultValue(defaultValue);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.LINK)) {
                storageSourceParamDef.setLink(link);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.LINK_NAME)) {
                storageSourceParamDef.setLinkName(linkName);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.TYPE)) {
                storageSourceParamDef.setType(type);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.OPTIONS)) {
                storageSourceParamDef.setOptions(optionsList);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.OPTION_ALLOW_CREATE)) {
                storageSourceParamDef.setOptionAllowCreate(optionAllowCreate);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.ORDER)) {
                storageSourceParamDef.setOrder(order);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.PRO)) {
                storageSourceParamDef.setPro(pro);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.CONDITION)) {
                storageSourceParamDef.setCondition(condition);
            }
            if (fieldOverrideSetIsEmpty || !fieldOverrideSet.contains(StorageParamItemAnnoEnum.HIDDEN)) {
                storageSourceParamDef.setHidden(hidden);
            }
            storageSourceParamDefMap.put(fieldName, storageSourceParamDef);
            useFieldNames.add(fieldName);

            StorageParamItemAnnoEnum[] storageParamItemAnnoEnumArray = storageParamItemAnnotation.onlyOverwrite();
            if (ArrayUtils.isNotEmpty(storageParamItemAnnoEnumArray)) {
                Set<StorageParamItemAnnoEnum> set = fieldOverrideMap.getOrDefault(fieldName, new HashSet<>());
                set.addAll(Arrays.asList(storageParamItemAnnoEnumArray));
                fieldOverrideMap.put(fieldName, set);
            }
        }

        // 按照顺序排序
        ArrayList<StorageSourceParamDef> result = new ArrayList<>(storageSourceParamDefMap.values());
        result.sort(Comparator.comparingInt(StorageSourceParamDef::getOrder));

        // 写入到缓存中
        STORAGE_SOURCE_PARAM_CACHE.put(clazz, result);
        return result;
    }

    /**
     * 从注解中获取 options 列表
     *
     * @param   storageParamItemAnnotation
     *          存储源参数注解
     *
     * @return  options 列表，如果没有则返回空列表，不会返回 null
     */
    private static List<StorageSourceParamDef.Options> getOptionsList(StorageParamItem storageParamItemAnnotation, IStorageParam storageParam) {
        // 如果不是默认的空接口实现，优先从实现类中通过反射获取 options 列表
        Class<? extends StorageParamSelect> storageParamSelectClass = storageParamItemAnnotation.optionsClass();
        if (BooleanUtils.isNotTrue(storageParamSelectClass.isInterface())) {
            StorageParamSelect storageParamSelect = ReflectUtil.newInstance(storageParamSelectClass);
            List<StorageSourceParamDef.Options> options = storageParamSelect.getOptions(storageParamItemAnnotation, storageParam);
            if (CollectionUtils.isEmpty(options)) {
                return Collections.emptyList();
            }
            return options;
        }

        // 从注解中获取 options
        List<StorageSourceParamDef.Options> optionsList = new ArrayList<>();
        StorageParamSelectOption[] options = storageParamItemAnnotation.options();
        if (ArrayUtils.isNotEmpty(options)) {
            for (StorageParamSelectOption storageParamSelectOption : options) {
                StorageSourceParamDef.Options option = new StorageSourceParamDef.Options(storageParamSelectOption);
                optionsList.add(option);
            }
        }
        return optionsList;
    }

    /**
     * 解析注解中的 link 字段, 如果不为空, 且不是 http 或 https 开头, 则认为是相对地址，添加站点域名为开头
     *
     * @param   storageParamItemAnnotation
     *          存储源参数注解
     *
     * @return  解析后的 link 字段
     */
    private static String parseAnnotationLinkField(StorageParamItem storageParamItemAnnotation) {
        String link = storageParamItemAnnotation.link();
        // 如果不为空，且不是 http 或 https 开头，则添加站点域名开头
        if (StringUtils.isNotEmpty(link) && !link.toLowerCase().startsWith(StringUtils.HTTP)) {
            SystemConfigService systemConfigService = SpringUtil.getBean(SystemConfigService.class);
            String domain = systemConfigService.getAxiosFromDomainOrSetting();
            link = StringUtils.concat(domain, link);
        }
        return link;
    }

}