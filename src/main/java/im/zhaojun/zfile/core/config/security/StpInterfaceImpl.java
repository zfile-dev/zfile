package im.zhaojun.zfile.core.config.security;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.convert.Convert;
import im.zhaojun.zfile.module.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author zhaojun
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private static final List<String> ADMIN_ROLE_LIST = Collections.singletonList("admin");

    public static final List<String> EMPTY_ROLE_LIST = Collections.emptyList();

    @Resource
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合，这里没用到这个功能，所以返回空集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        boolean isAdmin = userService.isAdmin(Convert.toInt(loginId));
        return isAdmin ? ADMIN_ROLE_LIST : EMPTY_ROLE_LIST;
    }

}