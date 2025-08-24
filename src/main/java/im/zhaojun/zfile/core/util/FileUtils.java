package im.zhaojun.zfile.core.util;

import org.apache.commons.io.FilenameUtils;

/**
 * 文件相关工具类
 *
 * @author zhaojun
 */
public class FileUtils {

    public static String getName(final String fileName) {
        if (fileName == null) {
            return null;
        }

        int i = fileName.lastIndexOf(CharSequenceUtil.SLASH_CHAR);
        if (i >= 0 && i <= fileName.length() - 1) {
            return fileName.substring(i + 1);
        }

        return fileName;
    }

    public static String getParentPath(final String fileName) {
        String fullPathNoEndSeparator = FilenameUtils.getFullPathNoEndSeparator(StringUtils.trimEndSlashes(fileName));
        if (fullPathNoEndSeparator == null || fullPathNoEndSeparator.isEmpty()) {
            return StringUtils.SLASH;
        }
        return fullPathNoEndSeparator;
    }

    public static String getExtension(final String fileName) throws IllegalArgumentException {
        if (fileName == null) {
            return null;
        }

        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            return fileName.substring(i + 1);
        }

        return "";
    }

}