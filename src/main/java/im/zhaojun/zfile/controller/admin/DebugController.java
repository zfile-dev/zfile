package im.zhaojun.zfile.controller.admin;


import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class DebugController {

    @Value("${zfile.debug}")
    private Boolean debug;

    @Resource
    private SystemConfigService systemConfigService;

    @ResponseBody
    @GetMapping("/debug/resetPwd")
    public ResultBean resetPwd() {
        if (debug) {
            systemConfigService.updateUsernameAndPwd("admin", "123456");
            return ResultBean.success();
        } else {
            return ResultBean.error("未开启 DEBUG 模式，不允许进行此操作。");
        }
    }

}