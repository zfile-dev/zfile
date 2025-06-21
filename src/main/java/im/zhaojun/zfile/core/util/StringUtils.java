package im.zhaojun.zfile.core.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 字符串相关工具类
 *
 * @author zhaojun
 */
public class StringUtils extends CharSequenceUtil implements StrPool {

    public static final String HTTP = "http";

    public static final String PROTOCOL_MARKER = "://";

    private static final LRUCache<String, String> CACHE = CacheUtil.newLRUCache(1000);

    /**
     * 移除 URL 中的前后的所有 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1/file1/', 返回 'folder1/file1'
     *          如 path = '///folder1/file1//', 返回 'folder1/file1'
     */
    public static String trimSlashes(String path) {
        path = trimStartSlashes(path);
        path = trimEndSlashes(path);
        return path;
    }


    /**
     * 移除 URL 中的前面的所有 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1/file1', 返回 'folder1/file1'
     *          如 path = '//folder1/file1', 返回 'folder1/file1'
     *
     */
    public static String trimStartSlashes(String path) {
        if (isEmpty(path)) {
            return path;
        }

        while (path.startsWith(SLASH)) {
            path = path.substring(1);
        }

        return path;
    }


    /**
     * 移除 URL 中结尾的所有 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1/file1/', 返回 '/folder1/file1'
     *          如 path = '/folder1/file1///', 返回 '/folder1/file1'
     */
    public static String trimEndSlashes(String path) {
        if (isEmpty(path)) {
            return path;
        }

        while (path.endsWith(SLASH)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }


    /**
     * 去除路径中所有重复的 '/'，如果最开始的协议头前有 / 也一并去除。
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1//file1/', 返回 '/folder1/file1/'
     *          如 path = '/folder1////file1///', 返回 '/folder1/file1/'
     */
    public static String removeDuplicateSlashes(String path) {
        if (isEmpty(path)) {
            return path;
        }

        return CACHE.get(path, false, () -> {
            StringBuilder sb = new StringBuilder(path.length());
            int protocolIndex = path.indexOf(PROTOCOL_MARKER);

            int pathStartIndex = 0;

            // 1. 处理协议部分
            if (protocolIndex > -1) {
                // 找到协议名称的实际开始位置
                int schemeStartIndex = 0;
                while (schemeStartIndex < protocolIndex && path.charAt(schemeStartIndex) == '/') {
                    schemeStartIndex++;
                }

                sb.append(path, schemeStartIndex, protocolIndex);
                sb.append(PROTOCOL_MARKER);

                pathStartIndex = protocolIndex + PROTOCOL_MARKER.length();
            }

            if (pathStartIndex < path.length()) {
                char lastChar;
                char firstPathChar = path.charAt(pathStartIndex);
                sb.append(firstPathChar);
                lastChar = firstPathChar;

                for (int i = pathStartIndex + 1; i < path.length(); i++) {
                    char current = path.charAt(i);
                    if (current != SLASH_CHAR || lastChar != SLASH_CHAR) {
                        sb.append(current);
                        lastChar = current;
                    }
                }
            }

            return sb.toString();
        });
    }


    /**
     * 去除路径中所有重复的 '/', 并且去除开头的 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1//file1/', 返回 'folder1/file1/'
     *          如 path = '///folder1////file1///', 返回 'folder1/file1/'
     */
    public static String removeDuplicateSlashesAndTrimStart(String path) {
        path = removeDuplicateSlashes(path);
        path = trimStartSlashes(path);
        return path;
    }


    /**
     * 去除路径中所有重复的 '/', 并且去除结尾的 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1//file1/', 返回 '/folder1/file1'
     *          如 path = '///folder1////file1///', 返回 '/folder1/file1'
     */
    public static String removeDuplicateSlashesAndTrimEnd(String path) {
        path = removeDuplicateSlashes(path);
        path = trimEndSlashes(path);
        return path;
    }


    /**
     * 拼接 URL，并去除重复的分隔符 '/'，并去除开头的 '/', 但不会影响 http:// 和 https:// 这种头部.
     *
     * @param   strs
     *          拼接的字符数组
     *
     * @return  拼接结果
     */
    public static String concatTrimStartSlashes(String... strs) {
        return trimStartSlashes(concat(strs));
    }


    /**
     * 拼接 URL，并去除重复的分隔符 '/'，并去除结尾的 '/', 但不会影响 http:// 和 https:// 这种头部.
     *
     * @param   strs
     *          拼接的字符数组
     *
     * @return  拼接结果
     */
    public static String concatTrimEndSlashes(String... strs) {
        return trimEndSlashes(concat(strs));
    }


    /**
     * 拼接 URL，并去除重复的分隔符 '/'，并去除开头和结尾的 '/', 但不会影响 http:// 和 https:// 这种头部.
     *
     * @param   strs
     *          拼接的字符数组
     *
     * @return  拼接结果
     */
    public static String concatTrimSlashes(String... strs) {
        return trimSlashes(concat(strs));
    }


    /**
     * 拼接 URL，并去除重复的分隔符 '/'，但不会影响 http:// 和 https:// 这种头部.
     *
     * @param   strs
     *          拼接的字符数组
     *
     * @return  拼接结果
     */
    public static String concat(String... strs) {
        StringBuilder sb = new StringBuilder(SLASH);
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (isEmpty(str)) {
                continue;
            }
            sb.append(str);
            if (i != strs.length - 1) {
                sb.append(SLASH_CHAR);
            }
        }
        return removeDuplicateSlashes(sb.toString());
    }


    /**
     * 拼接 URL，并去除重复的分隔符 '/'，但不会影响 http:// 和 https:// 这种头部.
     *
     * @param   encodeAllIgnoreSlashes
     *          是否 encode 编码 (忽略 /)
     *
     * @param   strs
     *          拼接的字符数组
     *
     * @return  拼接结果
     */
    public static String concat(boolean encodeAllIgnoreSlashes, String... strs) {
        String res = concat(strs);
        if (encodeAllIgnoreSlashes) {
            return encodeAllIgnoreSlashes(res);
        } else {
            return res;
        }
    }


    /**
     * 替换 URL 中的 Host 部分，如替换 http://a.com/1.txt 为 https://abc.com/1.txt
     *
     * @param   originUrl
     *          原 URL
     *
     * @param   replaceHost
     *          替换的 HOST
     *
     * @return  替换后的 URL
     */
    public static String replaceHost(String originUrl, String replaceHost) {
        try {
            String path = new URL(originUrl).getFile();
            return concat(replaceHost, path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 编码 URL，默认使用 UTF-8 编码
     * URL 的 Fragment URLEncoder
     * 默认的编码器针对Fragment，定义如下：
     *
     * <pre>
     * fragment    = *( pchar / "/" / "?" )
     * pchar       = unreserved / pct-encoded / sub-delims / ":" / "@"
     * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * sub-delims  = "!" / "$" / "&amp;" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
     * </pre>
     *
     * 具体见：https://datatracker.ietf.org/doc/html/rfc3986#section-3.5
     *
     * @param   url
     *          被编码内容
     *
     * @return  编码后的字符
     */
    public static String encode(String url) {
        return URLEncodeUtil.encodeFragment(url);
    }


    /**
     * 编码全部字符
     *
     * @param   str
     *          被编码内容
     *
     * @return  编码后的字符
     */
    public static String encodeAllIgnoreSlashes(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder();

        int prevIndex = -1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == StringUtils.SLASH_CHAR) {
                if (prevIndex < i) {
                    String substring = str.substring(prevIndex + 1, i);
                    sb.append(URLEncodeUtil.encodeAll(substring));
                    prevIndex = i;
                }
                sb.append(c);
            }

            if (i == str.length() - 1 && prevIndex < i) {
                String substring = str.substring(prevIndex + 1, i + 1);
                sb.append(URLEncodeUtil.encodeAll(substring));
            }
        }

        return sb.toString();
    }


    /**
     * 解码 URL, 默认使用 UTF8 编码. 不会将 + 转为空格.
     *
     * @param   url
     *          被解码内容
     *
     * @return  解码后的内容
     */
    public static String decode(String url) {
        return URLUtil.decode(url, StandardCharsets.UTF_8, false);
    }


    /**
     * 移除字符串中所有换行符并去除前后空格
     *
     * @param   str
     *          URL
     *
     * @return  移除协议后的 URL
     */
    public static String removeAllLineBreaksAndTrim(String str) {
        String removeResult = StrUtil.removeAllLineBreaks(str);
        return trim(removeResult);
    }


    /**
     * 移除字符串前后空格
     *
     * @param   str
     *          字符串
     *
     * @return  移除前后空格后的字符串
     */
    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }


    /**
     * 如果给定字符串不是以suffix结尾的，在尾部补充 suffix
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 补充后的字符串
     */
    public static String addSuffixIfNot(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str.toString();
        }

        if (str.toString().endsWith(suffix.toString())) {
            return str.toString();
        }

        return str.toString() + suffix;
    }

    /**
     * 是否包含特定字符，忽略大小写，如果给定两个参数都为{@code null}，返回true
     *
     * @param str     被检测字符串
     * @param testStr 被测试是否包含的字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(CharSequence str, CharSequence testStr) {
        if (null == str) {
            // 如果被监测字符串和
            return null == testStr;
        }
        return StrUtil.indexOfIgnoreCase(str, testStr) > -1;
    }


    /**
     * 指定范围内查找指定字符
     *
     * @param   str
     *          字符串
     *
     * @param   searchChar
     *          被查找的字符
     *
     * @return  位置
     */
    public static int indexOf(String str, char searchChar) {
        if (isEmpty(str)) {
            return INDEX_NOT_FOUND;
        }
        return str.indexOf(searchChar);
    }


    /**
     * 字符串驼峰转下划线格式
     *
     * @param   param
     *          驼峰格式字符串
     *
     * @return  下划线格式字符串
     */
    public static String camelToUnderline(String param) {
        if (isEmpty(param)) {
            return EMPTY;
        }

        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append(UNDERLINE);
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }


    /**
     * 强制给 URL 设置协议
     *
     * @param   url
     *          URL 地址，可以是带协议的，也可以是不带协议的，写会忽略大小写
     *
     * @param   schema
     *          协议，如 http, https, http://, https://
     *
     * @return  设置协议后的 URL
     */
    public static String setSchema(String url, String schema) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(schema)) {
            return url;
        }

        if (!schema.endsWith("://")) {
            schema += "://";
        }

        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith("http://")) {
            url = url.substring(7);
        } else if (lowerUrl.startsWith("https://")) {
            url = url.substring(8);
        }

        return schema + url;
    }

}