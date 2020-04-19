package im.zhaojun.zfile.controller.admin;

import im.zhaojun.zfile.model.dto.DriveConfigDTO;
import im.zhaojun.zfile.model.dto.ResultBean;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.service.DriveConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 驱动器 Controller
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class DriveController {

    @Resource
    private DriveConfigService driveConfigService;


    /**
     * 获取所有驱动器列表
     *
     * @return  驱动器列表
     */
    @GetMapping("drives")
    public ResultBean driveList() {
        List<DriveConfig> list = driveConfigService.list();
        return ResultBean.success(list);
    }


    /**
     * 获取指定驱动器基本信息及其参数
     *
     * @param   id
     *          驱动器 ID
     *
     * @return  驱动器基本信息信息
     */
    @GetMapping("drive/{id}")
    public ResultBean driveItem(@PathVariable Integer id) {
        DriveConfigDTO driveConfig = driveConfigService.findDriveConfigDTOById(id);
        return ResultBean.success(driveConfig);
    }


    /**
     * 保存驱动器设置
     */
    @PostMapping("drive")
    public ResultBean saveDriveItem(@RequestBody DriveConfigDTO driveConfigDTO) {
        driveConfigService.save(driveConfigDTO);
        return ResultBean.success();
    }


    /**
     * 删除驱动器设置
     *
     * @param   id
     *          驱动器 ID
     */
    @DeleteMapping("drive/{id}")
    public ResultBean deleteDriveItem(@PathVariable Integer id) {
        driveConfigService.deleteById(id);
        return ResultBean.success();
    }

}