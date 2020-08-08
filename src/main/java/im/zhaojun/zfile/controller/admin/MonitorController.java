package im.zhaojun.zfile.controller.admin;

import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.model.support.SystemMonitorInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控 Controller
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
public class MonitorController {


    /**
     * 获取系统监控信息
     */
    @GetMapping("monitor")
    public ResultBean monitor() {
        return ResultBean.success(new SystemMonitorInfo());
    }

}