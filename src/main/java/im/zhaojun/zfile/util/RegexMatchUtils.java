package im.zhaojun.zfile.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配工具类
 *
 * @author me
 * @date 2022/4/9
 */
public class RegexMatchUtils {

    /**
     * 正则匹配分组序号值
     *
     * @param regex 正则表达式
     * @param str   待匹配字符串
     * @param index 分组序号,从1开始
     * @return {@link String} 不存在/匹配失败返回null
     */
    public static String matchByIndex(String regex, String str, Integer index) {
        Matcher matcher = match(regex, str);
        if (matcher == null) {
            return null;
        }
        return getIndexResult(matcher, index);
    }

    /**
     * 匹配字符串
     *
     * @param regex 正则表达式
     * @param str   待匹配字符串
     * @return {@link Matcher}
     */
    public static Matcher match(String regex, String str) {
        if (str == null || "".equals(str)) {
            return null;
        }
        Matcher matcher = Pattern.compile(regex).matcher(str);
        if (!matcher.lookingAt()) {
            return null;
        }
        return matcher;
    }

    /**
     * 获取指定分组序号的匹配结果
     *
     * @param matcher {@link Matcher}
     * @param index   分组序号,从1开始
     * @return {@link String} 不存在/匹配失败返回null
     */
    public static String getIndexResult(Matcher matcher, Integer index) {
        if (matcher == null || index == null || index < 0 || index > matcher.groupCount()) {
            return null;
        }
        return matcher.group(index);
    }
}
