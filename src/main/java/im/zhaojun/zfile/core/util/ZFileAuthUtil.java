package im.zhaojun.zfile.core.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import im.zhaojun.zfile.module.share.context.ShareAccessContext;
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

        // 检查是否为分享访问，如果是则返回分享者用户 ID
        if (ShareAccessContext.isShareAccess()) {
            Integer shareUserId = ShareAccessContext.getShareUserId();
            return userService.getById(shareUserId);
        }

        return userService.getById(StpUtil.getLoginId(UserConstant.ANONYMOUS_ID));
	}

	public static Integer getCurrentUserId() {
		if (userService == null) {
			userService = SpringUtil.getBean(UserService.class);
		}

        // 检查是否为分享访问，如果是则返回分享者用户 ID
        if (ShareAccessContext.isShareAccess()) {
            return ShareAccessContext.getShareUserId();
        }

        try {
			return StpUtil.getLoginId(UserConstant.ANONYMOUS_ID);
		} catch (Exception e) {
			return UserConstant.ANONYMOUS_ID;
		}
	}
	
}