package im.zhaojun.zfile.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.ApiLimit;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.ZFileAuthUtil;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.user.model.entity.User;
import im.zhaojun.zfile.module.user.model.enums.LoginVerifyModeEnum;
import im.zhaojun.zfile.module.user.model.request.ResetAdminUserNameAndPasswordRequest;
import im.zhaojun.zfile.module.user.model.request.UpdateUserPwdRequest;
import im.zhaojun.zfile.module.user.model.request.UserLoginRequest;
import im.zhaojun.zfile.module.user.model.result.CheckLoginResult;
import im.zhaojun.zfile.module.user.model.result.LoginResult;
import im.zhaojun.zfile.module.user.model.result.LoginVerifyImgResult;
import im.zhaojun.zfile.module.user.service.UserService;
import im.zhaojun.zfile.module.user.service.DynamicLoginEntryService;
import im.zhaojun.zfile.module.user.service.login.ImgVerifyCodeService;
import im.zhaojun.zfile.module.user.service.login.LoginService;
import im.zhaojun.zfile.module.user.util.LoginEntryPathUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;

@Slf4j
@Tag(name = "用户接口")
@ApiSort(6)
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private LoginService loginService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ImgVerifyCodeService imgVerifyCodeService;

    @Resource
    private ZFileProperties zFileProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void initSecureLoginEntry() throws NoSuchMethodException {
        Method doLoginMethod = UserController.class.getMethod("doLogin", UserLoginRequest.class);
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        String secureLoginEntry = systemConfigDTO.getSecureLoginEntry();
        RequestMappingInfo requestMappingInfo = dynamicLoginEntryService.buildLoginRequestMappingInfo(secureLoginEntry);
        dynamicLoginEntryService.registerMappingHandlerMapping(SystemConfig.SECURE_LOGIN_ENTRY_NAME, requestMappingInfo, this, doLoginMethod);
        log.info("注册安全登录入口成功，当前登录路径为: {} ", LoginEntryPathUtils.resolveLoginPath(secureLoginEntry));
    }

    @Resource
    private DynamicLoginEntryService dynamicLoginEntryService;

    @ApiOperationSupport(order = 0)
    @Operation(summary = "校验安全登录入口")
    @GetMapping("/login/entry/validate")
    public AjaxJson<Void> validateLoginEntry(@RequestParam(value = "entry", required = false, defaultValue = "") String entry) {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        boolean matched = systemConfigDTO.getSecureLoginEntry() == null || StringUtils.equals(systemConfigDTO.getSecureLoginEntry(), entry);
        if (!matched) {
            return AjaxJson.getError("安全登录入口不正确");
        }
        return AjaxJson.getSuccess();
    }

    @ApiOperationSupport(order = 1, ignoreParameters = {"zfile-token"})
    @Operation(summary = "登录")
    @ApiLimit(timeout = 60, maxCount = 10)
    public AjaxJson<LoginResult> doLogin(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        // 进行登录验证，如果验证失败，会抛出异常
        loginService.verify(userLoginRequest);

        // 获取用户的上下文信息, 并登录
        User user = userService.getByUsername(userLoginRequest.getUsername());
        Integer userId = user.getId();
        StpUtil.login(userId);

        // 返回登录结果
        boolean isAdmin = userService.isAdmin(userId);
        LoginResult loginResult = new LoginResult(StpUtil.getTokenInfo().getTokenValue(), isAdmin);
        return AjaxJson.getSuccess("登录成功", loginResult);
    }

    @ApiOperationSupport(order = 2)
    @Operation(summary = "注销")
    @PostMapping("/logout")
    public AjaxJson<Void> logout() {
        StpUtil.logout();
        return AjaxJson.getSuccess("注销成功");
    }

    @ApiOperationSupport(order = 3)
    @Operation(summary = "获取登陆验证方式")
    @GetMapping("/login/verify-mode")
    public AjaxJson<LoginVerifyModeEnum> loginVerifyMode(String username) {
        LoginVerifyModeEnum loginVerifyModeEnum = LoginVerifyModeEnum.OFF_MODE;

        // 判断是否开启图形验证码
        SystemConfigDTO systemConfig = systemConfigService.getSystemConfig();
        Boolean loginImgVerify = systemConfig.getLoginImgVerify();
        if (BooleanUtils.isTrue(loginImgVerify)) {
            loginVerifyModeEnum = LoginVerifyModeEnum.IMG_VERIFY_MODE;
        }

        // 判断是否是管理员, 并且开启了二次验证
        boolean isAdmin = userService.isAdmin(username);
        boolean enableTwoFactorAuth = BooleanUtils.isTrue(systemConfig.getAdminTwoFactorVerify());
        boolean loginVerifySecretNotBlank = StringUtils.isNotBlank(systemConfig.getLoginVerifySecret());
        if (isAdmin && enableTwoFactorAuth && loginVerifySecretNotBlank) {
            loginVerifyModeEnum = LoginVerifyModeEnum.TWO_FACTOR_AUTHENTICATION_MODE;
        }
        return AjaxJson.getSuccessData(loginVerifyModeEnum);
    }


    @ApiOperationSupport(order = 4)
    @Operation(summary = "获取图形验证码")
    @GetMapping("/login/captcha")
    public AjaxJson<LoginVerifyImgResult> captcha() {
        LoginVerifyImgResult loginVerifyImgResult = imgVerifyCodeService.generatorCaptcha();
        return AjaxJson.getSuccessData(loginVerifyImgResult);
    }


    @ApiOperationSupport(order = 5)
    @Operation(summary = "检测是否已登录")
    @GetMapping("/login/check")
    public AjaxJson<CheckLoginResult> checkLogin() {
        CheckLoginResult checkLoginResult = new CheckLoginResult();
        checkLoginResult.setIsLogin(StpUtil.isLogin());
        if (checkLoginResult.getIsLogin()) {
            checkLoginResult.setIsAdmin(StpUtil.hasRole("admin"));
            User currentUser = ZFileAuthUtil.getCurrentUser();
            if (currentUser != null) {
                checkLoginResult.setNickname(currentUser.getNickname());
                checkLoginResult.setUsername(currentUser.getUsername());
            }
        }

        return AjaxJson.getSuccessData(checkLoginResult);
    }

    @SaCheckLogin
    @ApiOperationSupport(order = 6)
    @PostMapping("/updatePwd")
    @Operation(summary = "修改用户密码")
    @ResponseBody
    @DemoDisable
    public AjaxJson<Void> updatePwd(@RequestBody @Valid UpdateUserPwdRequest updateUserPwdRequest) {
        userService.updateUserNameAndPwdById(ZFileAuthUtil.getCurrentUserId(), updateUserPwdRequest);
        return AjaxJson.getSuccess();
    }

    @ResponseBody
    @ApiOperationSupport(order = 7)
    @Operation(summary = "重置管理员密码", description = "开启 debug 模式时，访问此接口会强制将管理员账户密码修改为用户指定值")
    @PutMapping("/resetAdminPassword")
    @DemoDisable
    public AjaxJson<Void> resetPwd(@RequestBody @Valid ResetAdminUserNameAndPasswordRequest requestObj) {
        if (!zFileProperties.isDebug()) {
            log.warn("当前为非调试模式, 无法重置管理员登录信息");
            throw new BizException(ErrorCode.BIZ_ERROR);
        }
        userService.resetAdminLoginInfo(requestObj);
        systemConfigService.resetLoginVerifyMode();
        return AjaxJson.getSuccess();
    }

}