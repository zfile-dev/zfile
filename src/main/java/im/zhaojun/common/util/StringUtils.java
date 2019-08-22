package im.zhaojun.common.util;

public class StringUtils {

    /**
     * 移除 URL 中的第一个 '/'
     * @return 如 path = '/folder1/file1', 返回 'folder1/file1'
     */
    public static String removeFirstSeparator(String path) {
        if (!"".equals(path) && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 移除 URL 中的最后一个 '/'
     * @return 如 path = '/folder1/file1/', 返回 '/folder1/file1'
     */
    public static String removeLastSeparator(String path) {
        if (!"".equals(path) && path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * 将域名和路径组装成 URL, 主要用来处理分隔符 '/'
     * @param domain    域名
     * @param path      路径
     * @return          URL
     */
    public static String concatPath(String domain, String path) {
        if (path != null && path.length() > 1 && path.charAt(0) != '/') {
            path = '/' + path;
        }

        if (domain.charAt(domain.length() - 1) == '/') {
            domain = domain.substring(0, domain.length() - 2);
        }

        return domain + path;
    }
}
