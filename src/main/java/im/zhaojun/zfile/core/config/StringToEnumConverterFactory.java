package im.zhaojun.zfile.core.config;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * String 转枚举通用转换器工厂
 *
 * @author zhaojun
 */
@Slf4j
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    /**
     * 存储枚举类型的缓存
     */
    private static final Map<Class<?>, Converter<String, ? extends Enum<?>>> CONVERTER_MAP = new ConcurrentHashMap<>();

    /**
     * 枚举类的获取枚举值方法缓存
     */
    private static final Map<Class<?>, Method> TABLE_METHOD_OF_ENUM_TYPES = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked cast")
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        // 缓存转换器
        Converter<String, T> converter = (Converter<String, T>) CONVERTER_MAP.get(targetType);
        if (converter == null) {
            converter = new StringToEnumConverter<>(targetType);
            CONVERTER_MAP.put(targetType, converter);
        }
        return converter;
    }

    static class StringToEnumConverter<T extends Enum<?>> implements Converter<String, T> {

        private final Map<String, T> enumMap = new ConcurrentHashMap<>();

        StringToEnumConverter(Class<T> enumType) {
            Method method = getMethod(enumType);
            T[] enums = enumType.getEnumConstants();

            // 将值与枚举对象对应并缓存
            for (T e : enums) {
                try {
                    enumMap.put(method.invoke(e).toString(), e);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    log.error("获取枚举值错误!!! ", ex);
                }
            }
        }


        @Override
        public T convert(@NotNull String source) {
            // 获取
            T t = enumMap.get(source);
            if (t == null) {
                throw new IllegalArgumentException("该字符串找不到对应的枚举对象 字符串:" + source);
            }
            return t;
        }
    }


    public static <T> Method getMethod(Class<T> enumType) {
        Method method;
        // 找到取值的方法
        if (IEnum.class.isAssignableFrom(enumType)) {
            try {
                method = enumType.getMethod("getValue");
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("类:%s 找不到 getValue方法",
                        enumType.getName()));
            }
        } else {
            method = TABLE_METHOD_OF_ENUM_TYPES.computeIfAbsent(enumType, k -> {
                Field field =
                        dealEnumType(enumType).orElseThrow(() -> new IllegalArgumentException(String.format(
                                "类:%s 找不到 EnumValue注解", enumType.getName())));

                Class<?> fieldType = field.getType();
                String fieldName = field.getName();
                String methodName =  StringUtils.concatCapitalize(boolean.class.equals(fieldType) ? "is" : "get", fieldName);
                try {
                    return enumType.getDeclaredMethod(methodName);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            });
        }
        return method;
    }


    private static Optional<Field> dealEnumType(Class<?> clazz) {
        return clazz.isEnum() ?
                Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(EnumValue.class)).findFirst() : Optional.empty();
    }

}