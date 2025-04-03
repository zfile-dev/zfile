package im.zhaojun.zfile.core.util;

import cn.hutool.core.lang.func.Func1;

import javax.annotation.Nullable;
import java.util.*;

public class CollectionUtils {


    /**
     * 判断集合是否为空
     *
     * @param   collection
     *          集合
     *
     * @return  是否为空
     */
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }


    /**
     * 判断集合是否不为空
     *
     * @param   collection
     *          集合
     *
     * @return  是否不为空
     */
    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !isEmpty(collection);
    }


    /**
     * 从集合中获取第一个元素, 如果集合为空则返回 {@code null}
     *
     * @param   list
     *          集合，可能为 {@code null}
     *
     * @return  第一个元素，如果集合为空则返回 {@code null}
     */
    @Nullable
    public static <T> T getFirst(@Nullable List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }


    /**
     * 从集合中获取最后一个元素, 如果集合为空则返回 {@code null}
     *
     * @param   list
     *          集合，可能为 {@code null}
     *
     * @return  最后一个元素，如果集合为空则返回 {@code null}
     */
    @Nullable
    public static <T> T getLast(@Nullable List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }


    /**
     * 加入全部
     *
     * @param   <T>
     *          集合元素类型
     *
     * @param   collection
     *          被加入的集合 {@link Collection}
     *
     * @param   values
     *          要加入的内容数组
     *
     * @return  原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, T[] values) {
        if (null != collection && null != values) {
            Collections.addAll(collection, values);
        }
        return collection;
    }


    /**
     * Iterable 转换为 Map, 根据指定的 keyFunc 函数生成 Key. Value 为 Iterable 中的元素.<br>
     * 可以指定将结果放入的 Map, 如不指定则会新建一个 HashMap 返回.
     *
     * @param   <K>
     *          Map Key 类型
     *
     * @param   <V>
     *          Map Value 类型
     *
     * @param   values
     *          被转换的 Iterable
     *
     * @param   map
     *          转换后的 Value 存放的 Map, 如果为 {@code null} 则新建一个 HashMap
     *
     * @param   keyFunc
     *          生成 Map 的 Key 的函数
     *
     * @return  转换后的 Map
     */
    public static <K, V> Map<K, V> toMap(final @Nullable Iterable<V> values, final @Nullable Map<K, V> map, final @Nullable Func1<V, K> keyFunc) {
        if (values == null || keyFunc == null) {
            return Collections.emptyMap();
        }

        final Map<K, V> result = map == null ? new HashMap<>() : map;

        for (V value : values) {
            try {
                result.put(keyFunc.call(value), value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

}