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

    public static String concatURL(String path, String name) {
        return removeDuplicateSeparator("/" + path + "/" + name);
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

    public static String removeDuplicateSeparator(String path) {
        if (path == null || path.length() < 2) {
            return path;
        }

        StringBuilder sb = new StringBuilder();

        if (path.indexOf("http://") == 0) {
            sb.append("http://");
        } else if (path.indexOf("https://") == 0) {
            sb.append("http://");
        }

        for (int i = sb.length(); i < path.length() - 1; i++) {
            char current = path.charAt(i);
            char next = path.charAt(i + 1);
            if (!(current == '/' && next == '/')) {
                sb.append(current);
            }
        }
        sb.append(path.charAt(path.length() - 1));
        return sb.toString();
    }

}
