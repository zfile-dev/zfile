package im.zhaojun.zfile.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSizeConverter {

    private static final long KB_FACTOR = 1024L;
    private static final long MB_FACTOR = 1024L * KB_FACTOR;
    private static final long GB_FACTOR = 1024L * MB_FACTOR;
    private static final long TB_FACTOR = 1024L * GB_FACTOR;
    private static final long PB_FACTOR = 1024L * TB_FACTOR;

    private static final Pattern FILE_SIZE_PATTERN = Pattern.compile("([\\d.]+)\\s*([a-zA-Z]+)");

    public static long convertFileSizeToBytes(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }

        Matcher matcher = FILE_SIZE_PATTERN.matcher(sizeStr.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("无效的文件大小格式: " + sizeStr);
        }

        String valueStr = matcher.group(1);
        String unitStr = matcher.group(2).toUpperCase();

        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的数字格式: " + valueStr, e);
        }

        if (value < 0) {
            throw new IllegalArgumentException("文件大小不能为负数: " + valueStr);
        }

        long multiplier = switch (unitStr) {
            case "B" ->
                    1L;
            case "KB", "KIB" ->
                    KB_FACTOR;
            case "MB", "MIB" ->
                    MB_FACTOR;
            case "GB", "GIB" ->
                    GB_FACTOR;
            case "TB", "TIB" ->
                    TB_FACTOR;
            case "PB", "PIB" ->
                    PB_FACTOR;
            default -> throw new IllegalArgumentException("不支持的单位: " + unitStr + " (支持 B, KB, MB, GB, TB, PB)");
        };

        double bytesDouble = value * multiplier;
        if (bytesDouble > Long.MAX_VALUE) {
            throw new ArithmeticException("转换后的字节数超过了 Long 类型的最大值: " + bytesDouble);
        }

        return Math.round(bytesDouble);
    }

}