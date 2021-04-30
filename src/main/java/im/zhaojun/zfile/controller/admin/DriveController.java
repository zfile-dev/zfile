package im.zhaojun.zfile.controller.admin;

import com.alibaba.fastjson.JSONObject;
import im.zhaojun.zfile.model.dto.DriveConfigDTO;
import im.zhaojun.zfile.model.entity.DriveConfig;
import im.zhaojun.zfile.model.entity.FilterConfig;
import im.zhaojun.zfile.model.support.ResultBean;
import im.zhaojun.zfile.service.DriveConfigService;
import im.zhaojun.zfile.service.FilterConfigService;
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
 * 驱动器相关操作 Controller
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
public class DriveController {

    @Resource
    private DriveConfigService driveConfigService;

    @Resource
    private FilterConfigService filterConfigService;


    /**
     * 获取所有驱动器列表
     *
     * @return  驱动器列表
     */
    @GetMapping("/drives")
    public ResultBean driveList() {
        List<DriveConfig> list = driveConfigService.list();
        return ResultBean.success(list);
    }


    /**
     * 获取指定驱动器基本信息及其参数
     *
     * @param   driveId
     *          驱动器 ID
     *
     * @return  驱动器基本信息
     */
    @GetMapping("/drive/{driveId}")
    public ResultBean driveItem(@PathVariable Integer driveId) {
        DriveConfigDTO driveConfig = driveConfigService.findDriveConfigDTOById(driveId);
        return ResultBean.success(driveConfig);
    }


    /**
     * 保存驱动器设置
     */
    @PostMapping("/drive")
    public ResultBean saveDriveItem(@RequestBody DriveConfigDTO driveConfigDTO) {
        driveConfigService.saveDriveConfigDTO(driveConfigDTO);
        return ResultBean.success();
    }


    /**
     * 删除驱动器设置
     *
     * @param   driveId
     *          驱动器 ID
     */
    @DeleteMapping("/drive/{driveId}")
    public ResultBean deleteDriveItem(@PathVariable Integer driveId) {
        driveConfigService.deleteById(driveId);
        return ResultBean.success();
    }


    /**
     * 启用驱动器
     *
     * @param   driveId
     *          驱动器 ID
     */
    @PostMapping("/drive/{driveId}/enable")
    public ResultBean enable(@PathVariable Integer driveId) {
        DriveConfig driveConfig = driveConfigService.findById(driveId);
        driveConfig.setEnable(true);
        driveConfigService.updateDriveConfig(driveConfig);
        return ResultBean.success();
    }


    /**
     * 停止驱动器
     *
     * @param   driveId
     *          驱动器 ID
     */
    @PostMapping("/drive/{driveId}/disable")
    public ResultBean disable(@PathVariable Integer driveId) {
        DriveConfig driveConfig = driveConfigService.findById(driveId);
        driveConfig.setEnable(false);
        driveConfigService.updateDriveConfig(driveConfig);
        return ResultBean.success();
    }


    /**
     * 根据驱动器 ID 获取过滤文件列表
     *
     * @param   driveId
     *          驱动器 ID
     */
    @GetMapping("/drive/{driveId}/filters")
    public ResultBean getFilters(@PathVariable Integer driveId) {
        return ResultBean.success(filterConfigService.findByDriveId(driveId));
    }


    /**
     * 停止驱动器
     *
     * @param   driveId
     *          驱动器 ID
     */
    @PostMapping("/drive/{driveId}/filters")
    public ResultBean saveFilters(@RequestBody List<FilterConfig> filter, @PathVariable Integer driveId) {
        filterConfigService.batchSave(filter, driveId);
        return ResultBean.success();
    }


    /**
     * 保存拖拽排序信息
     *
     * @param   driveConfigs
     *          拖拽排序信息
     */
    @PostMapping("/drive/drag")
    public ResultBean saveDriveDrag(@RequestBody List<JSONObject> driveConfigs) {
        driveConfigService.saveDriveDrag(driveConfigs);
        return ResultBean.success();
    }


    /**
     * 更新驱动器 ID
     *
     * @param   updateId
     *          驱动器原 ID
     *
     * @param   newId
     *          驱动器新 ID
     */
    @PostMapping("/drive/updateId")
    public ResultBean updateDriveId(Integer updateId, Integer newId) {
        DriveConfig driveConfig = driveConfigService.findById(newId);
        if (driveConfig != null) {
            return ResultBean.error("已存在的 ID，请更换 ID 后重试。");
        }
        driveConfigService.updateId(updateId, newId);
        return ResultBean.success();
    }

}