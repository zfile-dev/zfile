package im.zhaojun.zfile.core.util;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.core.constant.ZFileConstant;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 字符串相关工具类
 *
 * @author zhaojun
 */
public class StringUtils {

    public static final char DELIMITER = '/';

    public static final String DELIMITER_STR = "/";

    public static final String HTTP_PROTOCOL = "http://";

    public static final String HTTPS_PROTOCOL = "https://";


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
     * 移除 URL 中的第一个 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1/file1', 返回 'folder1/file1'
     *          如 path = '/folder1/file1', 返回 'folder1/file1'
     *
     */
    public static String trimStartSlashes(String path) {
        if (StrUtil.isEmpty(path)) {
            return path;
        }

        while (path.startsWith(DELIMITER_STR)) {
            path = path.substring(1);
        }

        return path;
    }


    /**
     * 移除 URL 中的最后一个 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1/file1/', 返回 '/folder1/file1'
     *          如 path = '/folder1/file1///', 返回 '/folder1/file1'
     */
    public static String trimEndSlashes(String path) {
        if (StrUtil.isEmpty(path)) {
            return path;
        }

        while (path.endsWith(DELIMITER_STR)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }


    /**
     * 去除路径中所有重复的 '/'
     *
     * @param   path
     *          路径
     *
     * @return  如 path = '/folder1//file1/', 返回 '/folder1/file1/'
     *          如 path = '/folder1////file1///', 返回 '/folder1/file1/'
     */
    public static String removeDuplicateSlashes(String path) {
        if (StrUtil.isEmpty(path)) {
            return path;
        }

        StringBuilder sb = new StringBuilder();

        // 是否包含 http 或 https 协议信息
        boolean containProtocol =  StrUtil.containsAnyIgnoreCase(path, HTTP_PROTOCOL, HTTPS_PROTOCOL);

        if (containProtocol) {
            path = trimStartSlashes(path);
        }

        // 是否包含 http 协议信息
        boolean startWithHttpProtocol = StrUtil.startWithIgnoreCase(path, HTTP_PROTOCOL);
        // 是否包含 https 协议信息
        boolean startWithHttpsProtocol = StrUtil.startWithIgnoreCase(path, HTTPS_PROTOCOL);

        if (startWithHttpProtocol) {
            sb.append(HTTP_PROTOCOL);
        } else if (startWithHttpsProtocol) {
            sb.append(HTTPS_PROTOCOL);
        }

        for (int i = sb.length(); i < path.length() - 1; i++) {
            char current = path.charAt(i);
            char next = path.charAt(i + 1);
            if (!(current == DELIMITER && next == DELIMITER)) {
                sb.append(current);
            }
        }
        sb.append(path.charAt(path.length() - 1));
        return sb.toString();
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
        StringBuilder sb = new StringBuilder(DELIMITER_STR);
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (StrUtil.isEmpty(str)) {
                continue;
            }
            sb.append(str);
            if (i != strs.length - 1) {
                sb.append(DELIMITER);
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
        StringBuilder sb = new StringBuilder(DELIMITER_STR);
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (StrUtil.isEmpty(str)) {
                continue;
            }
            sb.append(str);
            if (i != strs.length - 1) {
                sb.append(DELIMITER);
            }
        }
        if (encodeAllIgnoreSlashes) {
            return encodeAllIgnoreSlashes(removeDuplicateSlashes(sb.toString()));
        } else {
            return removeDuplicateSlashes(sb.toString());
        }
    }


    /**
     * 拼接文件直链生成 URL
     *
     * @param   storageKey
     *          存储源 ID
     *
     * @param   fullPath
     *          文件全路径
     *
     * @return  生成结果
     */
    public static String generatorPathLink(String storageKey, String fullPath) {
        SystemConfigService systemConfigService = SpringUtil.getBean(SystemConfigService.class);
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        String domain = systemConfig.getDomain();
        String directLinkPrefix = systemConfig.getDirectLinkPrefix();
        return concat(domain, directLinkPrefix, storageKey, encodeAllIgnoreSlashes(fullPath));
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
        if (StrUtil.isEmpty(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder();

        int prevIndex = -1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ZFileConstant.PATH_SEPARATOR_CHAR) {
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
     * 获取路径的上级目录, 如最后为 /, 则也会认为是一级目录
     *
     * @param   path
     *          文件路径
     *
     * @return  父级目录
     */
    public static String getParentPath(String path) {
        int toIndex = StrUtil.lastIndexOfIgnoreCase(path, ZFileConstant.PATH_SEPARATOR);
        if (toIndex <= 0) {
            return "/";
        } else {
            return StrUtil.sub(path, 0, toIndex);
        }
    }
    
    public static String removeAllLineBreaksAndTrim(String str) {
        String removeResult = StrUtil.removeAllLineBreaks(str);
        return StrUtil.trim(removeResult);
    }

}