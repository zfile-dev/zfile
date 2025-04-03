package im.zhaojun.zfile.module.user.service.login.verify.impl;

import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.user.model.request.UserLoginRequest;
import im.zhaojun.zfile.module.user.service.UserService;
import im.zhaojun.zfile.module.user.service.login.TwoFactorAuthenticatorVerifyService;
import im.zhaojun.zfile.module.user.service.login.verify.LoginVerifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(2)
public class TwoFactorAuthLoginVerifyService implements LoginVerifyService {

    @Resource
    private TwoFactorAuthenticatorVerifyService twoFactorAuthVerifyService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UserService userService;

    @Override
    public void verify(UserLoginRequest userLoginRequest) {
        // 如果不是管理员, 则不需要进行二次验证
        if (!userService.isAdmin(userLoginRequest.getUsername())) {
            return;
        }

        // 判断是否开启管理员二次验证
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        boolean disable2FA = BooleanUtils.isNotTrue(systemConfig.getAdminTwoFactorVerify());
        boolean empty2FASecret = StringUtils.isBlank(systemConfig.getLoginVerifySecret());
        if (disable2FA || empty2FASecret) {
            return;
        }

        String loginVerifySecret = systemConfig.getLoginVerifySecret();
        String verifyCode = userLoginRequest.getVerifyCode();
        twoFactorAuthVerifyService.checkCode(loginVerifySecret, verifyCode);
    }

}
