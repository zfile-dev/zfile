package im.zhaojun.zfile.core.util;

import cn.hutool.core.util.NumberUtil;

/**
 * 文件大小或带宽大小转可读单位
 *
 * @author zhaojun
 */
public class SizeToStrUtils {

    /**
     * 将文件大小转换为可读单位
     *
     * @param   bytes
     *          字节数
     *
     * @return  文件大小可读单位
     */
    public static String bytesToSize(long bytes) {
        if (bytes == 0) {
            return "0";
        }

        double k = 1024;
        String[] sizes = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        double i = Math.floor(Math.log(bytes) / Math.log(k));
        return NumberUtil.round(bytes / Math.pow(k, i), 3) + " " + sizes[(int) i];
    }


    /**
     * 将带宽大小转换为可读单位
     *
     * @param   bps
     *          字节数
     *
     * @return  带宽大小可读单位
     */
    public static String bpsToSize(long bps) {
        if (bps == 0) {
            return "0";
        }

        double k = 1000;
        String[] sizes = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        double i = Math.floor(Math.log(bps) / Math.log(k));
        return NumberUtil.round(bps / Math.pow(k, i), 3) + " " + sizes[(int) i];
    }

}