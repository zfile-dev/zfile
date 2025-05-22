package im.zhaojun.zfile.module.sso.controller;

import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.sso.model.response.SsoLoginItemResponse;
import im.zhaojun.zfile.module.sso.service.SsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 单点登录接口
 *
 * @author OnEvent
 */
@Slf4j
@Tag(name = "单点登录接口")
@RestController
@RequestMapping("/api/sso")
@RequiredArgsConstructor
public class SsoAPIController {

    private final SsoService ssoService;

    @GetMapping("/list")
    @Operation(summary = "登录页面 SSO 服务商列表")
    public AjaxJson<List<SsoLoginItemResponse>> list() {
        return AjaxJson.getSuccessData(ssoService.listAllLoginItems());
    }

}
