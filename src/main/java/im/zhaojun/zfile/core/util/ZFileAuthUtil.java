package im.zhaojun.zfile.core.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.user.model.constant.UserConstant;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.service.UserService;

/**
 * 登录认证工具类
 *
 * @author zhaojun
 */
public class ZFileAuthUtil {
	
	private static UserService userService;

	public static User getCurrentUser() {
		if (userService == null) {
			userService = SpringUtil.getBean(UserService.class);
		}

		return userService.getById(StpUtil.getLoginId(UserConstant.ANONYMOUS_ID));
	}

	public static Integer getCurrentUserId() {
		if (userService == null) {
			userService = SpringUtil.getBean(UserService.class);
		}

		try {
			return StpUtil.getLoginId(UserConstant.ANONYMOUS_ID);
		} catch (Exception e) {
			return UserConstant.ANONYMOUS_ID;
		}
	}
	
}