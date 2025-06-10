package im.zhaojun.zfile.module.sso.controller;

import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.sso.model.entity.SsoConfig;
import im.zhaojun.zfile.module.sso.model.request.CheckProviderDuplicateRequest;
import im.zhaojun.zfile.module.sso.service.SsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * 单点登录管理接口
 *
 * @author OnEvent
 */
@Slf4j
@Tag(name = "单点登录管理接口")
@RestController
@RequestMapping("/admin/sso")
@RequiredArgsConstructor
class SsoManagerController {

    private final SsoService ssoService;

    @GetMapping("/providers")
    @Operation(summary = "SSO 服务商列表")
    public AjaxJson<Collection<SsoConfig>> list() {
        List<SsoConfig> ssoConfigList = ssoService.list();
        return AjaxJson.getSuccessData(ssoConfigList);
    }

    @PostMapping("/provider")
    @Operation(summary = "保存 SSO 服务商")
    @DemoDisable
    public AjaxJson<SsoConfig> saveOrUpdateProvider(@RequestBody @Valid SsoConfig ssoConfig) {
        return AjaxJson.getSuccessData(ssoService.saveOrUpdate(ssoConfig));
    }

    @DeleteMapping("/provider/{provider}")
    @Operation(summary = "删除 SSO 服务商")
    @DemoDisable
    public AjaxJson<Void> deleteProvider(@PathVariable String provider) {
        ssoService.deleteProvider(provider);
        return AjaxJson.getSuccess();
    }

    @GetMapping("/provider/checkDuplicate")
    @Operation(summary = "检查服务商简称是否重复")
    public AjaxJson<Boolean> checkDuplicate(CheckProviderDuplicateRequest request) {
        Integer id = request.getId();
        String provider = request.getProvider();
        return AjaxJson.getSuccessData(ssoService.checkDuplicateProvider(id, provider));
    }

}