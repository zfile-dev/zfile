package im.zhaojun.zfile.module.user.service.login.verify.impl;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.request.UserLoginRequest;
import im.zhaojun.zfile.module.user.service.UserService;
import im.zhaojun.zfile.module.user.service.login.verify.LoginVerifyService;
import im.zhaojun.zfile.module.user.utils.PasswordVerifyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Objects;

@Slf4j
@Service
@Order(3)
public class PasswordVerifyService implements LoginVerifyService {

    @Resource
    private UserService userService;

    @Override
    public void verify(UserLoginRequest userLoginRequest) {
        User dbUser = userService.getByUsername(userLoginRequest.getUsername());
        if (dbUser == null) {
            throw new BizException(ErrorCode.BIZ_LOGIN_ERROR);
        }

        String dbPassword = dbUser.getPassword();
        String dbSalt = dbUser.getSalt();
        String requestPassword = userLoginRequest.getPassword();

        if (!PasswordVerifyUtils.verify(dbPassword, dbSalt, requestPassword)) {
            throw new BizException(ErrorCode.BIZ_LOGIN_ERROR);
        }
    }

}