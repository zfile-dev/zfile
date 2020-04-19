package im.zhaojun.zfile.controller.admin;

import im.zhaojun.zfile.model.dto.ResultBean;
import im.zhaojun.zfile.service.DriveConfigService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 缓存 Controller
 *
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin/cache")
public class CacheController {

    @Resource
    private DriveConfigService driveConfigService;


    @PostMapping("/{driveId}/enable")
    public ResultBean enableCache(@PathVariable("driveId") Integer driveId) {
        driveConfigService.updateCacheStatus(driveId, true);
        return ResultBean.success();
    }

    @PostMapping("/{driveId}/disable")
    public ResultBean disableCache(@PathVariable("driveId") Integer driveId) {
        driveConfigService.updateCacheStatus(driveId, false);
        return ResultBean.success();
    }

}