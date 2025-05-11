package im.zhaojun.zfile.module.sso.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.module.sso.service.SsoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@Tag(name = "单点登录")
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
class SsoController
{
    private final SsoService ssoService;

    @GetMapping("/login/{provider}")
    public RedirectView login(@PathVariable String provider, HttpSession session)
    {
        var state = IdUtil.fastSimpleUUID();
        session.setAttribute("state", state);

        var url = ssoService.getAuthRedirectUrl(state);

        var redirect = new RedirectView();
        redirect.setUrl(url);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);

        return redirect;
    }

    @GetMapping("/login/callback")
    public RedirectView callback(@RequestParam("code") String code, @RequestParam("state") String state, HttpSession session)
    {
        if (!state.equals(session.getAttribute("state").toString()))
        {
            var err = URLUtil.encode("state 参数不一致");
            return new RedirectView("/sso/error?err=" + err);
        }

        var url = ssoService.callbackHandler(code);

        var redirect = new RedirectView();
        redirect.setUrl(url);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);
        return redirect;
    }

    @GetMapping("/login/success")
    public String success()
    {
        return "单点登录成功, 当前用户 ID: [" + StpUtil.getLoginIdAsString() + "]!";
    }

    @GetMapping("/login/error")
    public String error(@RequestParam("err") String err)
    {
        return "单点登录失败: " + err;
    }
}
