package im.zhaojun.zfile.module.storage.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.core.util.ClassUtils;
import im.zhaojun.zfile.core.util.PlaceholderUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelect;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceParamDef;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.IStorageParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractBaseFileService;
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

        ArrayList<StorageSourceParamDef> result = new ArrayList<>();

        // 获取存储源实现类的泛型参数类型
        Class<?> paramClass = ClassUtils.getClassFirstGenericsParam(clazz);
        Field[] fields = ReflectUtil.getFields(paramClass);

        // 已添加的字段列表.
        List<String> useFieldNames = new ArrayList<>();
        // 要忽略的字段名
        List<String> ignoreFieldNames = new ArrayList<>();

        for (Field field : fields) {
            // 获取字段上的注解
            StorageParamItem storageParamItemAnnotation = field.getAnnotation(StorageParamItem.class);
            if (storageParamItemAnnotation == null) {
                continue;
            }

            String key = storageParamItemAnnotation.key();
            int order = storageParamItemAnnotation.order();
            String name = storageParamItemAnnotation.name();
            String linkName = storageParamItemAnnotation.linkName();
            boolean required = storageParamItemAnnotation.required();
            String description = storageParamItemAnnotation.description();
            StorageParamTypeEnum type = storageParamItemAnnotation.type();
            String link = parseAnnotationLinkField(storageParamItemAnnotation);
            String defaultValue = PlaceholderUtils.resolvePlaceholdersBySpringProperties(storageParamItemAnnotation.defaultValue());

            // 取注解上标注的字段名称, 如果为空, 则使用字段名称
            if (StrUtil.isEmpty(key)) {
                key = field.getName();
            }

            // 如果字段已存在, 则跳过
            if (useFieldNames.contains(field.getName())) {
                continue;
            }

            // 如果字段被忽略, 则添加到忽略列表中
            if (storageParamItemAnnotation.ignoreInput()) {
                ignoreFieldNames.add(key);
            }

            // 如果默认值不为空, 则该字段则不是必填的
            if (StrUtil.isNotEmpty(defaultValue)) {
                required = false;
            }

            // 如果 type 为 select, 则获取 options 下拉列表.
            List<StorageSourceParamDef.Options> optionsList = getOptionsList(storageParamItemAnnotation, storageParam);

            StorageSourceParamDef storageSourceParamDef = StorageSourceParamDef.builder().
                    key(key).
                    name(name).
                    description(description).
                    required(required).
                    defaultValue(defaultValue).
                    link(link).
                    linkName(linkName).
                    type(type).
                    options(optionsList).
                    order(order).
                    build();

            if (!ignoreFieldNames.contains(key)) {
                result.add(storageSourceParamDef);
            }

            useFieldNames.add(field.getName());
        }

        // 按照顺序排序
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
        if (BooleanUtil.isFalse(storageParamSelectClass.isInterface())) {
            StorageParamSelect storageParamSelect = ReflectUtil.newInstance(storageParamSelectClass);
            List<StorageSourceParamDef.Options> options = storageParamSelect.getOptions(storageParamItemAnnotation, storageParam);
            if (CollUtil.isEmpty(options)) {
                return Collections.emptyList();
            }
            return options;
        }

        // 从注解中获取 options
        List<StorageSourceParamDef.Options> optionsList = new ArrayList<>();
        StorageParamSelectOption[] options = storageParamItemAnnotation.options();
        if (ArrayUtil.isNotEmpty(options)) {
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
        if (StrUtil.isNotEmpty(link) && !link.toLowerCase().startsWith(StringUtils.HTTP)) {
            SystemConfigService systemConfigService = SpringUtil.getBean(SystemConfigService.class);
            String domain = systemConfigService.getDomain();
            link = StringUtils.concat(domain, link);
        }
        return link;
    }

}