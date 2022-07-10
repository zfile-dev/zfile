package im.zhaojun.zfile.admin.controller.setting;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.admin.model.request.setting.UpdateLinkSettingRequest;
import im.zhaojun.zfile.admin.model.request.setting.UpdateSecuritySettingRequest;
import im.zhaojun.zfile.admin.model.request.setting.UpdateSiteSettingRequest;
import im.zhaojun.zfile.admin.model.request.setting.UpdateUserNameAndPasswordRequest;
import im.zhaojun.zfile.admin.model.request.setting.UpdateViewSettingRequest;
import im.zhaojun.zfile.admin.service.SystemConfigService;
import im.zhaojun.zfile.common.util.AjaxJson;
import im.zhaojun.zfile.home.model.dto.SystemConfigDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 站点设定值接口
 *
 * @author zhaojun
 */
@Api(tags = "站点设置模块")
@ApiSort(2)
@RestController
@RequestMapping("/admin")
public class SettingController {

    @Resource
    private SystemConfigService systemConfigService;

    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "获取站点信息",
            notes = "获取站点相关信息，如站点名称，风格样式，是否显示公告，是否显示文档区，自定义 CSS，JS 等参数")
    @GetMapping("/config")
    public AjaxJson<SystemConfigDTO> getConfig() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        return AjaxJson.getSuccessData(systemConfigDTO);
    }


    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "修改管理员账号密码")
    @PutMapping("/config/password")
    public AjaxJson<?> updatePwd(@Valid @RequestBody UpdateUserNameAndPasswordRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 3)
    @ApiOperation(value = "修改站点设置")
    @PutMapping("/config/site")
    public AjaxJson<?> updateSiteSetting(@Valid @RequestBody UpdateSiteSettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 4)
    @ApiOperation(value = "修改显示设置")
    @PutMapping("/config/view")
    public AjaxJson<?> updateViewSetting(@Valid @RequestBody UpdateViewSettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 5)
    @ApiOperation(value = "修改登陆安全设置")
    @PutMapping("/config/security")
    public AjaxJson<?> updateSecuritySetting(@Valid @RequestBody UpdateSecuritySettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }


    @ApiOperationSupport(order = 6)
    @ApiOperation(value = "修改直链设置")
    @PutMapping("/config/link")
    public AjaxJson<?> updateLinkSetting(@Valid @RequestBody UpdateLinkSettingRequest settingRequest) {
        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        BeanUtils.copyProperties(settingRequest, systemConfigDTO);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return AjaxJson.getSuccess();
    }

}