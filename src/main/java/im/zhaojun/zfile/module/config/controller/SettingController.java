package im.zhaojun.zfile.module.config.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.annotation.DemoDisable;
import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.config.model.request.*;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 站点设定值接口
 *
 * @author zhaojun
 */
@Tag(name = "站点设置模块")
@ApiSort(2)
@RestController
@RequestMapping("/admin")
public class SettingController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ZFileProperties zFileProperties;

    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取站点信息", description = "获取站点相关信息，如站点名称，风格样式，是否显示公告，是否显示文档区，自定义 CSS，JS 等参数")
    @GetMapping("/config")
    public AjaxJson<SystemConfigDTO> getConfig() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        if (zFileProperties != null && zFileProperties.isDemoSite()) {
            SystemConfigDTO copy = JSON.parseObject(JSON.toJSONString(systemConfigDTO), SystemConfigDTO.class);
            copy.setAuthCode(null);
            copy.setRsaHexKey(null);
            return AjaxJson.getSuccessData(copy);
        }
        return AjaxJson.getSuccessData(systemConfigDTO);
    }

    @ApiOperationSupport(order = 3)
    @Operation(summary = "修改站点设置")
    @PutMapping("/config/site")
    @DemoDisable
    public AjaxJson<Void> updateSiteSetting(@Valid @RequestBody UpdateSiteSettingRequest settingRequest) {
        if (StrUtil.length(settingRequest.getAuthCode()) > 36 && StrUtil.length(settingRequest.getAuthCode()) < 100) {
            throw new BizException("授权码长度异常，请检查是否额外复制了空格或特殊字符！");
        }
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 4)
    @Operation(summary = "修改显示设置")
    @PutMapping("/config/view")
    @DemoDisable
    public AjaxJson<Void> updateViewSetting(@Valid @RequestBody UpdateViewSettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 5)
    @Operation(summary = "修改登陆安全设置")
    @PutMapping("/config/security")
    @DemoDisable
    public AjaxJson<Void> updateSecuritySetting(@Valid @RequestBody UpdateSecuritySettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        if (BooleanUtils.isNotTrue(settingRequest.getAdminTwoFactorVerify())) {
            systemConfigDTO.setLoginVerifySecret("");
        }
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 6)
    @Operation(summary = "修改直链设置")
    @PutMapping("/config/link")
    @DemoDisable
    public AjaxJson<Void> updateLinkSetting(@Valid @RequestBody UpdateLinkSettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }

    @ApiOperationSupport(order = 7)
    @Operation(summary = "修改访问控制设置")
    @PutMapping("/config/access")
    @DemoDisable
    public AjaxJson<Void> updateSecuritySetting(@Valid @RequestBody UpdateAccessSettingRequest updateAccessSettingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(updateAccessSettingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }

}