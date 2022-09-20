package im.zhaojun.zfile.module.config.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.core.exception.InstallSystemException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.model.request.InstallSystemRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统初始化接口
 *
 * @author zhaojun
 */
@Api(tags = "初始化模块")
@RestController
@RequestMapping("/api")
public class InstallController {

    @Resource
    private SystemConfigService systemConfigService;


    @GetMapping("/install/status")
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取系统初始化状态", notes = "根据管理员用户名是否存在判断系统已初始化, 已初始化返回 true, 未初始化返回 false")
    public AjaxJson<Boolean> isInstall() {
        return AjaxJson.getSuccessData(systemConfigService.getSystemIsInstalled());
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "初始化系统", notes = "根据管理员用户名是否存在判断系统已初始化, 已初始化返回 true, 未初始化返回 false")
    @PostMapping("/install")
    public AjaxJson<Void> install(@RequestBody InstallSystemRequest installSystemRequest) {
        if (systemConfigService.getSystemIsInstalled()) {
            throw new InstallSystemException("请勿重复初始化");
        }

        installSystemRequest.setPassword(SecureUtil.md5(installSystemRequest.getPassword()));

        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtil.copyProperties(installSystemRequest, systemConfigDTO);
        systemConfigDTO.setInstalled(true);
        systemConfigService.updateSystemConfig(systemConfigDTO);

        return AjaxJson.getSuccess();
    }

}