package im.zhaojun.zfile.security;

import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.service.SystemConfigService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

/**
 * @author zhaojun
 */
public class MyUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 授权的时候是对角色授权，认证的时候应该基于资源，而不是角色，因为资源是不变的，而用户的角色是会变的
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        if (!Objects.equals(systemConfig.getUsername(), username)) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        return new User(systemConfig.getUsername(), systemConfig.getPassword(), Collections.emptyList());
    }

}