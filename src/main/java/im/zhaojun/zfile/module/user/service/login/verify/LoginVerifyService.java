package im.zhaojun.zfile.module.user.service.login.verify;

import im.zhaojun.zfile.module.user.model.request.UserLoginRequest;

public interface LoginVerifyService {

    void verify(UserLoginRequest userLoginRequest);

}
