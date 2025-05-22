package im.zhaojun.zfile.module.sso.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.module.sso.service.SsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 单点登录接口
 *
 * @author OnEvent
 */
@Slf4j
@Tag(name = "单点登录")
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
class SsoController {

    private final SsoService ssoService;

    @GetMapping("/{provider}/login")
    @Operation(summary = "获取单点登录地址")
    public RedirectView login(@PathVariable String provider, HttpSession session) {
        String state = IdUtil.fastSimpleUUID();
        session.setAttribute("state", state);

        String url = ssoService.getAuthRedirectUrl(provider, state);

        RedirectView redirect = new RedirectView();
        redirect.setUrl(url);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);
        return redirect;
    }

    @GetMapping("/{provider}/login/callback")
    @Operation(summary = "单点登录回调接口")
    public RedirectView callback(@PathVariable("provider") String provider, @RequestParam("code") String code, @RequestParam("state") String state, HttpSession session) {
        Object expectedState = session.getAttribute("state");
        if (expectedState == null) {
            String err = URLUtil.encode("当前会话中 state 为空，可能是请求地址和回调地址不一致");
            return new RedirectView("/sso/login/error?err=" + err);
        }
        if (!expectedState.equals(state)) {
            String err = URLUtil.encode("state 参数不一致，请检查请求地址和回调地址是否一致");
            return new RedirectView("/sso/login/error?err=" + err);
        }

        String url = ssoService.callbackHandler(provider, code);

        RedirectView redirect = new RedirectView();
        redirect.setUrl(url);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);
        return redirect;
    }

}