package im.zhaojun.zfile.module.user.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import dev.samstevens.totp.exceptions.QrGenerationException;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.user.model.request.VerifyLoginTwoFactorAuthenticatorRequest;
import im.zhaojun.zfile.module.user.model.result.LoginTwoFactorAuthenticatorResult;
import im.zhaojun.zfile.module.user.service.login.TwoFactorAuthenticatorVerifyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 登陆注销相关接口
 *
 * @author zhaojun
 */
@Tag(name = "登录模块")
@ApiSort(1)
@RestController
@RequestMapping("/admin")
public class AdminTwoFAController {

    @Resource
    private TwoFactorAuthenticatorVerifyService twoFactorAuthenticatorVerifyService;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "生成 2FA")
    @GetMapping("/2fa/setup")
    public AjaxJson<LoginTwoFactorAuthenticatorResult> setupDevice() throws QrGenerationException {
        LoginTwoFactorAuthenticatorResult loginTwoFactorAuthenticatorResult = twoFactorAuthenticatorVerifyService.setupDevice();
        return AjaxJson.getSuccessData(loginTwoFactorAuthenticatorResult);
    }


    @ApiOperationSupport(order = 2)
    @Operation(summary = "2FA 验证并绑定")
    @PostMapping("/2fa/verify")
    @DemoDisable
    public AjaxJson<Void> deviceVerify(@Valid @RequestBody VerifyLoginTwoFactorAuthenticatorRequest verifyLoginTwoFactorAuthenticatorRequest) {
        twoFactorAuthenticatorVerifyService.deviceVerify(verifyLoginTwoFactorAuthenticatorRequest);
        return AjaxJson.getSuccess();
    }

}