package im.zhaojun.zfile.controller.admin;

import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.service.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理后台接口
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public ResultBean getConfig() {
        SystemConfigDTO systemConfigDTO = systemConfigService.getSystemConfig();
        return ResultBean.success(systemConfigDTO);
    }


    /**
     * 更新系统配置
     */
    @PostMapping("/config")
    public ResultBean updateConfig(SystemConfigDTO systemConfigDTO) {
        systemConfigDTO.setId(1);
        systemConfigService.updateSystemConfig(systemConfigDTO);
        return ResultBean.success();
    }


    /**
     * 修改管理员登陆密码
     */
    @PostMapping("/update-pwd")
    public ResultBean updatePwd(String username, String password) {
        systemConfigService.updateUsernameAndPwd(username, password);
        return ResultBean.success();
    }

}