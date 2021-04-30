package im.zhaojun.zfile.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.service.SystemConfigService;

/**
 * @author zhaojun
 */
public class StringUtils {


    public static final char DELIMITER = '/';

    public static final String HTTP_PROTOCAL = "http://";

    public static final String HTTPS_PROTOCAL = "https://";

    /**
     * 移除 URL 中的第一个 '/'
     * @return 如 path = '/folder1/file1', 返回 'folder1/file1'
     */
    public static String removeFirstSeparator(String path) {
        if (!"".equals(path) && path.charAt(0) == DELIMITER) {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 移除 URL 中的最后一个 '/'
     * @return 如 path = '/folder1/file1/', 返回 '/folder1/file1'
     */
    public static String removeLastSeparator(String path) {
        if (!"".equals(path) && path.charAt(path.length() - 1) == DELIMITER) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String concatUrl(String path, String name) {
        return removeDuplicateSeparator(DELIMITER + path + DELIMITER + name);
    }


    /**
     * 将域名和路径组装成 URL, 主要用来处理分隔符 '/'
     * @param domain    域名
     * @param path      路径
     * @return          URL
     */
    public static String concatPath(String domain, String path) {
        if (path != null && path.length() > 1 && path.charAt(0) != DELIMITER) {
            path = DELIMITER + path;
        }

        if (domain != null && domain.charAt(domain.length() - 1) == DELIMITER) {
            domain = domain.substring(0, domain.length() - 2);
        }

        return domain + path;
    }

    public static String removeDuplicateSeparator(String path) {
        if (path == null || path.length() < 2) {
            return path;
        }

        StringBuilder sb = new StringBuilder();

        if (path.indexOf(HTTP_PROTOCAL) == 0) {
            sb.append(HTTP_PROTOCAL);
        } else if (path.indexOf(HTTPS_PROTOCAL) == 0) {
            sb.append(HTTPS_PROTOCAL);
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

    public static boolean isNullOrEmpty(String s) {
        return s == null || "".equals(s);
    }

    public static boolean isNotNullOrEmpty(String s) {
        return !isNullOrEmpty(s);
    }

    /**
     * 获取 basePath + path 的全路径地址.
     * @return basePath + path 的全路径地址.
     */
    public static String getFullPath(String basePath, String path) {
        basePath = ObjectUtil.defaultIfNull(basePath, "");
        path = ObjectUtil.defaultIfNull(path, "");
        return StringUtils.removeDuplicateSeparator(basePath + ZFileConstant.PATH_SEPARATOR + path);
    }

    /**
     * 替换 URL 中的 Host 部分，如替换 http://a.com/1.txt 为 https://abc.com/1.txt
     * @param   originUrl
     *          原 URL
     * @param   replaceHost
     *          替换的 HOST
     * @return  替换后的 URL
     */
    public static String replaceHost(String originUrl, String replaceHost) {
        return concatPath(replaceHost, URLUtil.getPath(originUrl));
    }

    /**
     * 拼接 URL，并去除重复的分隔符 '/'，但不会影响 http:// 和 https:// 这种头部
     * @param strs      拼接的字符数组
     * @return          拼接结果
     */
    public static String concatUrl(String... strs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]);
            if (i != strs.length - 1) {
                sb.append(DELIMITER);
            }
        }
        return removeDuplicateSeparator(sb.toString());
    }

    /**
     * 拼接文件直链生成 URL
     * @param driveId       驱动器 ID
     * @param fullPath      文件全路径
     * @return              生成结果
     */
    public static String generatorLink(Integer driveId, String fullPath) {
        SystemConfigService systemConfigService = SpringContextHolder.getBean(SystemConfigService.class);
        String domain = systemConfigService.getDomain();
        return concatUrl(domain, "directlink", String.valueOf(driveId), fullPath);
    }
}
