package im.zhaojun.zfile.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
/**
 * 配置文件或模板中的占位符替换工具类
 *
 * @author zhaojun
 */
@Slf4j
public class PlaceholderUtils {

    /**
     * Prefix for system property placeholders: "${"
     */
    public static final String PLACEHOLDER_PREFIX = "${";
    /**
     * Suffix for system property placeholders: "}"
     */
    public static final String PLACEHOLDER_SUFFIX = "}";


    /**
     * 解析占位符, 将指定的占位符替换为指定的值. 变量值从 Spring 环境中获取, 如没取到, 则默认为空.
     *
     * 必须在 Spring 环境下使用, 否则会抛出异常.
     *
     *
     * @param   formatStr
     *          模板字符串
     *
     * @return  替换后的字符串
     */
    public static String resolvePlaceholdersBySpringProperties(String formatStr) {
        String placeholderName = getFirstPlaceholderName(formatStr);
        if (StrUtil.isEmpty(placeholderName)) {
            return formatStr;
        }

        String propertyValue = SpringUtil.getProperty(placeholderName);
        Map<String, String> map = new HashMap<>();
        map.put(placeholderName, propertyValue);
        return resolvePlaceholders(formatStr, map);
    }


    /**
     * 解析占位符, 将指定的占位符替换为指定的值.
     *
     * @param   formatStr
     *          模板字符串
     *
     * @param   parameter
     *          参数列表
     *
     * @return  替换后的字符串
     */
    public static String resolvePlaceholders(String formatStr, Map<String, String> parameter) {
        if (parameter == null || parameter.isEmpty()) {
            return formatStr;
        }
        StringBuffer buf = new StringBuffer(formatStr);
        int startIndex = buf.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                try {
                    String propVal = parameter.get(placeholder);
                    if (propVal != null) {
                        buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
                        nextIndex = startIndex + propVal.length();
                    } else {
                        log.warn("Could not resolve placeholder '{}' in [{}] ", placeholder, formatStr);
                    }
                } catch (Exception ex) {
                    log.error("Could not resolve placeholder '{}' in [{}]: ", placeholder, formatStr, ex);
                }
                startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
            } else {
                startIndex = -1;
            }
        }
        return buf.toString();
    }


    /**
     * 获取模板字符串第一个占位符的名称, 如 "我的名字是: ${name}, 我的年龄是: ${age}", 返回 "name".
     *
     * @param   formatStr
     *          模板字符串
     *
     * @return  占位符名称
     */
    public static String getFirstPlaceholderName(String formatStr) {
        List<String> list = getPlaceholderNames(formatStr);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }


    /**
     * 获取模板字符串第一个占位符的名称, 如 "我的名字是: ${name}, 我的年龄是: ${age}", 返回 ["name", "age].
     *
     * @param   formatStr
     *          模板字符串
     *
     * @return  占位符名称
     */
    public static List<String> getPlaceholderNames(String formatStr) {
        if (StrUtil.isEmpty(formatStr)) {
            return Collections.emptyList();
        }

        List<String> placeholderNameList = new ArrayList<>();

        StringBuffer buf = new StringBuffer(formatStr);
        int startIndex = buf.indexOf(PLACEHOLDER_PREFIX);
        while (startIndex != -1) {
            int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
                placeholderNameList.add(placeholder);
            } else {
                startIndex = -1;
            }
        }
        return placeholderNameList;
    }

}