package im.zhaojun.zfile.core.util;

import cn.hutool.core.text.StrSplitter;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * 字符串工具类
 *
 * @author zhaojun
 */
public class CharSequenceUtil implements CharPool {

    /**
     * 找不到索引时的返回值
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * 字符串常量：{@code "null"} <br>
     * 注意：{@code "null" != null}
     */
    public static final String NULL = "null";

    /**
     * 字符串常量：空字符串 {@code ""}
     */
    public static final String EMPTY = "";

    /**
     * 字符串常量：空格符 {@code " "}
     */
    public static final String SPACE = " ";


    /**
     * 获取 CharSequence 的长度, 如果为 null, 返回 0
     *
     * @param   ch
     *          要获取长度的 CharSequence, 可能为 null
     *
     * @return  CharSequence 的长度
     */
    public static int length(final @Nullable CharSequence ch) {
        return ch == null ? 0 : ch.length();
    }


    /**
     * {@link CharSequence} 转为字符串
     *
     * @param   cs
     *          {@link CharSequence}
     *
     * @return  字符串
     */
    public static String str(final @Nullable CharSequence cs) {
        return null == cs ? null : cs.toString();
    }


    /**
     * 判断 CharSequence 是否为空
     *
     * @param   cs
     *          {@link CharSequence}
     *
     * @return  是否为空
     */
    public static boolean isEmpty(final @Nullable CharSequence cs) {
        return cs == null || cs.isEmpty();
    }


    /**
     * CharSequence 是否不为空
     *
     * @param   cs
     *          {@link CharSequence}
     *
     * @return  是否不为空
     */
    public static boolean isNotEmpty(final @Nullable CharSequence cs) {
        return !isEmpty(cs);
    }


    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isAllEmpty()                  // true}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("", null)          // true}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("123", "")         // false}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code CharSequenceUtil.isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * @param   strs
     *          字符串列表
     *
     * @return  所有字符串是否都为空
     */
    public static boolean isAllEmpty(final @Nullable CharSequence... strs) {
        if (strs == null) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.hasEmpty()                  // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("", null)          // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("123", "")         // true}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code CharSequenceUtil.hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * @param   strs
     *          字符串列表
     *
     * @return  是否包含空字符串
     */
    public static boolean hasEmpty(final @Nullable CharSequence... strs) {
        if (ArrayUtils.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }


    /**
     * <p>指定字符串数组中的元素，是否都不为空字符串。</p>
     * <p>如果指定的字符串数组的长度不为 0，或者所有元素都不是空字符串，则返回 true。</p>
     * <br>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isAllNotEmpty()                  // false}</li>
     *     <li>{@code CharSequenceUtil.isAllNotEmpty("", null)          // false}</li>
     *     <li>{@code CharSequenceUtil.isAllNotEmpty("123", "")         // false}</li>
     *     <li>{@code CharSequenceUtil.isAllNotEmpty("123", "abc")      // true}</li>
     *     <li>{@code CharSequenceUtil.isAllNotEmpty(" ", "\t", "\n")   // true}</li>
     * </ul>
     *
     * @param   args
     *          字符串数组
     *
     * @return  所有字符串是否都不为为空白
     */
    public static boolean isAllNotEmpty(final @Nullable CharSequence... args) {
        return !hasEmpty(args);
    }


    /**
     * 字符串是否为空白
     *
     * @param   ch
     *          要判断的字符串, 可能为 null
     *
     * @return  是否为空白
     */
    public static boolean isBlank(final @Nullable CharSequence ch) {
        final int strLen = ch == null ? 0 : ch.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(ch.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 字符串是否不为空白
     *
     * @param   cs
     *          字符串
     *
     * @return  是否不为空白
     */
    public static boolean isNotBlank(final @Nullable CharSequence cs) {
        return !isBlank(cs);
    }


    /**
     * 比较两个 CharSequence 是否相等, 区分大小写, 如果两个都为 null, 返回 true
     *
     * @param   cs1
     *          CharSequence 1, 可能为 null
     *
     * @param   cs2
     *          CharSequence 2, 可能为 null
     *
     * @return  是否相等
     */
    public static boolean equals(final @Nullable CharSequence cs1, final @Nullable CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        // 逐个比较
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 比较两个 CharSequence 是否相等, 可以选择是否忽略大小写, 如果两个都为 null, 返回 true
     *
     * @param   cs1
     *          字符串 1
     *
     * @param   cs2
     *          字符串 2
     *
     * @param   ignoreCase
     *          是否忽略大小写
     *
     * @return  是否相等
     */
    public static boolean equals(final @Nullable CharSequence cs1,final @Nullable CharSequence cs2, boolean ignoreCase) {
        return ignoreCase ? equalsIgnoreCase(cs1, cs2) : equals(cs1, cs2);
    }


    /**
     * 字符串是否相等, 忽略大小写
     *
     * @param   cs1
     *          字符串 1
     *
     * @param   cs2
     *          字符串 2
     *
     * @return  忽略大小写后是否相等
     */
    public static boolean equalsIgnoreCase(final @Nullable CharSequence cs1, final @Nullable CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }

        return cs1.toString().equalsIgnoreCase(cs2.toString());
    }


    /**
     * 切分字符串，如果分隔符不存在则返回原字符串
     *
     * @param   str
     *          被切分的字符串
     *
     * @param   separator
     *          分隔符
     *
     * @return  字符串
     */
    public static List<String> split(final CharSequence str, final CharSequence separator) {
        return split(str, separator, false, false);
    }


    /**
     * 切分字符串
     *
     * @param   str
     *          被切分的字符串
     *
     * @param   separator
     *          分隔符字符
     *
     * @param   isTrim
     *          是否去除切分字符串后每个元素两边的空格
     *
     * @param   ignoreEmpty
     *          是否忽略空串
     *
     * @return  切分后的集合
     */
    public static List<String> split(CharSequence str, CharSequence separator, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, 0, isTrim, ignoreEmpty);
    }


    /**
     * 切分字符串
     *
     * @param   str
     *          被切分的字符串
     *
     * @param   separator
     *          分隔符字符
     *
     * @param   limit
     *          限制分片数，-1 不限制
     *
     * @param   isTrim
     *          是否去除切分字符串后每个元素两边的空格
     *
     * @param   ignoreEmpty
     *          是否忽略空串
     *
     * @return  切分后的集合
     */
    public static List<String> split(CharSequence str, CharSequence separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        final String separatorStr = (null == separator) ? null : separator.toString();
        return StrSplitter.split(str, separatorStr, limit, isTrim, ignoreEmpty);
    }


    /**
     * 指定字符串是否在字符串中出现过
     *
     * @param   str
     *          字符串
     *
     * @param   searchStr
     *          被查找的字符串
     *
     * @return  是否包含
     */
    public static boolean contains(final @Nullable CharSequence str, final @Nullable CharSequence searchStr) {
        if (null == str || null == searchStr) {
            return false;
        }
        return str.toString().contains(searchStr);
    }


    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     *
     * @param   str
     *          指定字符串
     *
     * @param   testStrs
     *          需要检查的字符串数组
     *
     * @return  是否包含任意一个字符串
     */
    public static boolean containsAny(final @Nullable CharSequence str, final @Nullable  CharSequence... testStrs) {
        if (isEmpty(str) || ArrayUtils.isEmpty(testStrs)) {
            return false;
        }
        for (CharSequence checkStr : testStrs) {
            if (null != checkStr && str.toString().contains(checkStr)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串<br>
     * 忽略大小写
     *
     * @param   str
     *          指定字符串
     *
     * @param   testStrs
     *          需要检查的字符串数组
     *
     * @return  是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(final @Nullable CharSequence str, final @Nullable CharSequence... testStrs) {
        return StringUtils.containsAnyIgnoreCase(str, testStrs);
    }


    /**
     * 以 conjunction 为分隔符将多个对象转换为字符串
     *
     * @param   conjunction
     *          分隔符
     *
     * @param   objs
     *          数组
     *
     * @return  连接后的字符串
     */
    public static String join(CharSequence conjunction, Object... objs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objs.length; i++) {
            Object item = objs[i];
            sb.append(item);
            if (i < objs.length - 1) {
                sb.append(conjunction);
            }
        }
        return sb.toString();
    }


    /**
     * 以 conjunction 为分隔符将 Collection 对象转换为字符串
     *
     * @param   conjunction
     *          分隔符
     *
     * @param   collection
     *          集合
     *
     * @return  连接后的字符串
     */
    public static String join(CharSequence conjunction, Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object item : collection) {
            sb.append(item).append(conjunction);
        }
        if (!sb.isEmpty()) {
            sb.delete(sb.length() - conjunction.length(), sb.length());
        }
        return sb.toString();
    }


    /**
     * 是否以指定字符串开头
     *
     * @param   str
     *          被监测字符串
     *
     * @param   prefix
     *          开头字符串
     *
     * @return  是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, false);
    }


    /**
     * 是否以指定字符串开头，忽略大小写
     *
     * @param   str
     *          被监测字符串
     *
     * @param   prefix
     *          开头字符串
     *
     * @return  是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
    }


    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param   str
     *          被监测字符串
     *
     * @param   prefix
     *          开头字符串
     *
     * @param   ignoreCase
     *          是否忽略大小写
     *
     * @return  是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean ignoreCase) {
        return startWith(str, prefix, ignoreCase, false);
    }


    /**
     * 是否以指定字符串开头<br>
     * 如果给定的字符串和开头字符串都为 null 则返回 true，否则任意一个值为 null 返回 false<br>
     * <pre>
     *     CharSequenceUtil.startWith("123", "123", false, true);   -- false
     *     CharSequenceUtil.startWith("ABCDEF", "abc", true, true); -- true
     *     CharSequenceUtil.startWith("abc", "abc", true, true);    -- false
     * </pre>
     *
     * @param   str
     *          被监测字符串
     *
     * @param   prefix
     *          开头字符串
     *
     * @param   ignoreCase
     *          是否忽略大小写
     *
     * @param   ignoreEquals
     *          是否忽略字符串相等的情况
     *
     * @return  是否以指定字符串开头
     */
    public static boolean startWith(final @Nullable CharSequence str, final @Nullable CharSequence prefix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == str || null == prefix) {
            if (ignoreEquals) {
                return false;
            }
            return null == str && null == prefix;
        }

        boolean isStartWith = str.toString()
                .regionMatches(ignoreCase, 0, prefix.toString(), 0, prefix.length());

        if (isStartWith) {
            return (!ignoreEquals) || (!equals(str, prefix, ignoreCase));
        }
        return false;
    }


    /**
     * 是否以指定字符串结尾
     *
     * @param   str
     *          被监测字符串
     *
     * @param   suffix
     *          结尾字符串
     *
     * @return  是否以指定字符串结尾
     */
    public static boolean endWith(final @Nullable CharSequence str, final @Nullable CharSequence suffix) {
        return endWith(str, suffix, false);
    }


    /**
     * 是否以指定字符串结尾<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param   str
     *          被监测字符串
     *
     * @param   suffix
     *          结尾字符串
     *
     * @param   ignoreCase
     *          是否忽略大小写
     *
     * @return  是否以指定字符串结尾
     */
    public static boolean endWith(final @Nullable CharSequence str, final @Nullable CharSequence suffix, boolean ignoreCase) {
        return endWith(str, suffix, ignoreCase, false);
    }


    /**
     * 是否以指定字符串结尾<br>
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param   str
     *          被监测字符串
     *
     * @param   suffix
     *          结尾字符串
     *
     * @param   ignoreCase
     *          是否忽略大小写
     *
     * @param   ignoreEquals
     *          是否忽略字符串相等的情况
     *
     * @return  是否以指定字符串结尾
     */
    public static boolean endWith(final @Nullable CharSequence str, final @Nullable CharSequence suffix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == str || null == suffix) {
            if (ignoreEquals) {
                return false;
            }
            return null == str && null == suffix;
        }

        final int strOffset = str.length() - suffix.length();
        boolean isEndWith = str.toString()
                .regionMatches(ignoreCase, strOffset, suffix.toString(), 0, suffix.length());

        if (isEndWith) {
            return (!ignoreEquals) || (!equals(str, suffix, ignoreCase));
        }
        return false;
    }


    /**
     * 去掉指定前缀
     *
     * @param   str
     *          字符串
     *
     * @param   prefix
     *          前缀
     *
     * @return  切掉后的字符串，若前缀不是 preffix， 返回原字符串
     */
    public static String removePrefix(final @Nullable CharSequence str, final @Nullable CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str(str);
        }
        String str2 = str.toString();
        String prefix2 = prefix.toString();
        if (str2.startsWith(prefix2)) {
            return str.subSequence(prefix.length(), str.length()).toString();
        }
        return str2; // 若前缀不是 prefix，返回原字符串
    }


    /**
     * 返回第一个非 {@code null} 元素
     *
     * @param   strs
     *          多个元素
     *
     * @param   <T>
     *          元素类型
     *
     * @return  第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T extends CharSequence> T firstNonNull(T... strs) {
        if (ArrayUtils.isNotEmpty(strs)) {
            for (T str : strs) {
                if (isNotEmpty(str)) {
                    return str;
                }
            }
        }
        return null;
    }


    /**
     * 截取分隔字符串之前的字符串，不包括分隔字符串<br>
     * 如果给定的字符串为空串（null或""）或者分隔字符串为null，返回原字符串<br>
     * 如果分隔字符串为空串""，则返回空串，如果分隔字符串未找到，返回原字符串，举例如下：
     *
     * <pre>
     * CharSequenceUtil.subBefore(null, *, false)      = null
     * CharSequenceUtil.subBefore("", *, false)        = ""
     * CharSequenceUtil.subBefore("abc", "a", false)   = ""
     * CharSequenceUtil.subBefore("abcba", "b", false) = "a"
     * CharSequenceUtil.subBefore("abc", "c", false)   = "ab"
     * CharSequenceUtil.subBefore("abc", "d", false)   = "abc"
     * CharSequenceUtil.subBefore("abc", "", false)    = ""
     * CharSequenceUtil.subBefore("abc", null, false)  = "abc"
     * </pre>
     *
     * @param   string
     *          被查找的字符串
     *
     * @param   separator
     *          分隔字符串（不包括）
     *
     * @param   isLastSeparator
     *          是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     *
     * @return  切割后的字符串
     */
    public static String subBefore(final @Nullable CharSequence string, final @Nullable CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string) || separator == null) {
            return null == string ? null : string.toString();
        }

        final String str = string.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return EMPTY;
        }
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (INDEX_NOT_FOUND == pos) {
            return str;
        }
        if (0 == pos) {
            return EMPTY;
        }
        return str.substring(0, pos);
    }

}