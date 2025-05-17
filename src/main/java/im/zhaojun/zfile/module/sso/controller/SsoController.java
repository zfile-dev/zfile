package im.zhaojun.zfile.module.sso.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.sso.model.entity.SsoConfig;
import im.zhaojun.zfile.module.sso.service.SsoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
class SsoController
{
    private final SsoService ssoService;

    @PostMapping("/provider")
    public AjaxJson<Void> insertProvider(@RequestBody @Valid SsoConfig provider)
    {
        return ssoService.insertProvider(provider);
    }

    @DeleteMapping("/provider/{provider}")
    public AjaxJson<Void> deleteProvider(@PathVariable String provider)
    {
        return ssoService.deleteProvider(provider);
    }

    @PutMapping("/provider")
    public AjaxJson<Void> modifyProvider(@RequestBody SsoConfig provider)
    {
        return ssoService.modifyProvider(provider);
    }

    @GetMapping("/provider/{provider}")
    public AjaxJson<?> getProvider(@PathVariable String provider)
    {
        return ssoService.getProvider(provider);
    }

    @GetMapping("/{provider}/login")
    public RedirectView login(@PathVariable String provider, HttpSession session)
    {
        var state = IdUtil.fastSimpleUUID();
        session.setAttribute("state", state);

        var url = ssoService.getAuthRedirectUrl(provider, state);

        var redirect = new RedirectView();
        redirect.setUrl(url);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);

        return redirect;
    }

    @GetMapping("/{provider}/login/callback")
    public RedirectView callback(@PathVariable("provider") String provider, @RequestParam("code") String code, @RequestParam("state") String state, HttpSession session)
    {
        if (!state.equals(session.getAttribute("state").toString()))
        {
            var err = URLUtil.encode("state 参数不一致");
            return new RedirectView("/sso/login/error?err=" + err);
        }

        var url = ssoService.callbackHandler(provider, code);

        var redirect = new RedirectView();
        redirect.setUrl(url);
        redirect.setStatusCode(HttpStatus.SEE_OTHER);
        return redirect;
    }

    @GetMapping("/login/success")
    public AjaxJson<Void> success()
    {
        return AjaxJson.getSuccess("单点登录成功, 当前用户 ID: [" + StpUtil.getLoginIdAsString() + "]!");
    }

    @GetMapping("/login/error")
    public AjaxJson<Void> error(@RequestParam("err") String err)
    {
        return AjaxJson.getError("单点登录失败: " + err);
    }
}
