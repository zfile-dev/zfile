package im.zhaojun.zfile.module.install.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.install.model.request.InstallSystemRequest;
import im.zhaojun.zfile.module.install.service.InstallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 系统初始化接口
 *
 * @author zhaojun
 */
@Tag(name = "初始化模块")
@RestController
@RequestMapping("/api")
public class InstallController {

    @Resource
    private InstallService installService;

    @GetMapping("/install/status")
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取系统初始化状态", description = "根据管理员用户名是否存在判断系统已初始化, 已初始化返回 true, 未初始化返回 false")
    public AjaxJson<Boolean> isInstall() {
        return AjaxJson.getSuccessData(installService.getSystemIsInstalled());
    }

    @ApiOperationSupport(order = 2)
    @Operation(summary = "初始化系统", description = "根据管理员用户名是否存在判断系统已初始化, 已初始化返回 true, 未初始化返回 false")
    @PostMapping("/install")
    @DemoDisable
    public AjaxJson<Void> install(@RequestBody InstallSystemRequest installSystemRequest) {
        installService.install(installSystemRequest);
        return AjaxJson.getSuccess();
    }

}