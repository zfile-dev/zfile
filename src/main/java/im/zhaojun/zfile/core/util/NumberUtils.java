package im.zhaojun.zfile.core.util;

/**
 * 数字工具类
 *
 * @author zhaojun
 */
public class NumberUtils {

    public static boolean isNullOrZero(Integer number) {
        return number == null || number == 0;
    }

    public static boolean isNotNullOrZero(Integer number) {
        return number != null && number != 0;
    }

}