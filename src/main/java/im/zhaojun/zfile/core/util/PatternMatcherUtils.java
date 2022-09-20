package im.zhaojun.zfile.core.util;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.constant.ZFileConstant;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则表达式工具类
 *
 * @author zhaojun
 */
public class PatternMatcherUtils {
	
	private static final Map<String, PathMatcher> PATH_MATCHER_MAP = new HashMap<>();
	
	/**
	 * 兼容模式的 glob 表达式匹配.
	 * 默认的 glob 表达式是不支持以下情况的:<br>
	 * <ul>
	 * <li>pattern: /a/**</li>
	 * <li>test1: /a</li>
	 * <li>test2: /a/</li>
	 * <ul>
	 * <p>test1 和 test 2 均无法匹配这种情况, 此方法兼容了这种情况, 即对 test 内容后拼接 "/xx", 使其可以匹配上 pattern.
	 * <p><strong>注意：</strong>但此方法对包含文件名的情况无效, 仅支持 test 为 路径的情况.
	 *
	 * @param 	pattern
	 *			glob 规则表达式
	 *
	 * @param 	test
	 *			匹配内容
	 *
	 * @return 	是否匹配.
	 */
	public static boolean testCompatibilityGlobPattern(String pattern, String test) {
		// 如果规则表达式最开始没有 /, 则兼容在最前方加上 /.
		if (!StrUtil.startWith(pattern, ZFileConstant.PATH_SEPARATOR)) {
			pattern = ZFileConstant.PATH_SEPARATOR + pattern;
		}
		
		// 兼容性处理.
		test = StringUtils.concat(test, ZFileConstant.PATH_SEPARATOR);
		if (StrUtil.endWith(pattern, "/**")) {
			test += "xxx";
		}
		return testGlobPattern(pattern, test);
	}
	
	
	/**
	 * 测试密码规则表达式和文件路径是否匹配
	 *
	 * @param 	pattern
	 *			glob 规则表达式
	 *
	 * @param 	test
	 *			测试字符串
	 */
	private static boolean testGlobPattern(String pattern, String test) {
		// 从缓存取出 PathMatcher, 防止重复初始化
		PathMatcher pathMatcher = PATH_MATCHER_MAP.getOrDefault(pattern, FileSystems.getDefault().getPathMatcher("glob:" + pattern));
		PATH_MATCHER_MAP.put(pattern, pathMatcher);
		
		return pathMatcher.matches(Paths.get(test)) || StrUtil.equals(pattern, test);
	}
	
}