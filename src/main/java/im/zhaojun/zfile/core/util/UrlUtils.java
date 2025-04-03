package im.zhaojun.zfile.core.util;

import cn.hutool.core.util.StrUtil;

/**
 * url 相关工具类
 *
 * @author zhaojun
 */
public class UrlUtils {

	/**
	 * 判断 URL 是否包含协议部分
	 *
	 * @param 	url
	 * 			URL 地址
	 *
	 * @return	是否包含协议部分
	 */
	public static boolean hasScheme(String url) {
		return url.startsWith("http://") || url.startsWith("https://");
	}

	/**
	 * 为 URL 拼接参数
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
		if (StringUtils.contains(url, "?")) {
			return url + "&" + name + "=" + value;
		} else {
			return url + "?" + name + "=" + value;
		}
	}

	/**
	 * 获取 URL 中的协议部分
	 *
	 * @param 	url
	 * 			URL 地址
	 *
	 * @return	协议部分
	 */
	public static String getSchema(String url) {
		if (StringUtils.startWithIgnoreCase(url, "http://")) {
			return "http";
		} else if (StringUtils.startWithIgnoreCase(url, "https://")) {
			return "https";
		} else {
			return "http";
		}
	}

	/**
	 * 移除 URL 中的协议部分
	 *
	 * @param 	url
	 * 			URL 地址
	 *
	 * @return	移除协议部分后的 URL
	 */
	public static String removeScheme(String url) {
		if (StringUtils.startWithIgnoreCase(url, "http://")) {
			url = url.substring(7);
		} else if (StringUtils.startWithIgnoreCase(url, "https://")) {
			url = url.substring(8);
		}

		return url;
	}

	/**
	 * 获取 URL 中的域名部分
	 *
	 * @param 	url
	 * 			URL 地址
	 *
	 * @return	域名部分
	 */
	public static String getDomain(String url) {
		if (!StringUtils.isEmpty(url)) {
			//替换指定前缀
			String newStr = url.replace("http://", "");
			newStr = newStr.replace("https://", "");

			int index = StrUtil.indexOf(newStr, '/');
			if (index > 0) {
				newStr = newStr.substring(0, index);
			}

			String[] split = newStr.split(":");
			if (split.length > 1) {
				return split[0];
			} else {
				return newStr;
			}
		} else {
			return null;
		}
	}

}