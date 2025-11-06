package im.zhaojun.zfile.module.user.util;

import im.zhaojun.zfile.core.util.StringUtils;

/**
 * 登录入口路径解析工具类。
 *
 * @author zhaojun
 */
public final class LoginEntryPathUtils {

    private LoginEntryPathUtils() {
    }
    public static final String DEFAULT_LOGIN_PATH = "/login";

    public static final String DEFAULT_LOGIN_API_PATH = "/user/login";

    /**
     * 根据安全入口配置值解析实际登录地址。
     *
     * @param secureLoginEntry 配置的安全入口值
     * @return 对应的登录路径
     */
    public static String resolveLoginPath(String secureLoginEntry) {
        if (StringUtils.isBlank(secureLoginEntry)) {
            return DEFAULT_LOGIN_PATH;
        }

        return DEFAULT_LOGIN_PATH + StringUtils.SLASH + secureLoginEntry;
    }

    /**
     * 根据安全入口配置值解析实际登录地址。
     *
     * @param secureLoginEntry 配置的安全入口值
     * @return 对应的登录路径
     */
    public static String resolveLoginApiPath(String secureLoginEntry) {
        if (StringUtils.isBlank(secureLoginEntry)) {
            return DEFAULT_LOGIN_API_PATH;
        }

        return DEFAULT_LOGIN_API_PATH + StringUtils.SLASH + secureLoginEntry;
    }

}