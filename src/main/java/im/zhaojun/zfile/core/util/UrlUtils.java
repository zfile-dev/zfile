package im.zhaojun.zfile.core.util;

import cn.hutool.core.util.StrUtil;

/**
 * url 相关工具类
 *
 * @author zhaojun
 */
public class UrlUtils {
	
	/**
	 * 给 url 拼接参数
	 *
	 * @param 	url
	 * 			原始 URL
	 *
	 * @param 	name
	 * 			参数名称
	 *
	 * @param 	value
	 * 			参数值
	 *
	 * @return	拼接后的 URL
	 */
	public static String concatQueryParam(String url, String name, String value) {
		if (StrUtil.contains(url, "?")) {
			return url + "&" + name + "=" + value;
		} else {
			return url + "?" + name + "=" + value;
		}
	}
	
}