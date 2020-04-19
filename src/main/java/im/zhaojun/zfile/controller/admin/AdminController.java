package im.zhaojun.zfile.controller.admin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ZipUtil;
import im.zhaojun.zfile.context.StorageTypeContext;
import im.zhaojun.zfile.model.dto.ResultBean;
import im.zhaojun.zfile.model.dto.StorageStrategyDTO;
import im.zhaojun.zfile.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.model.support.SystemMonitorInfo;
import im.zhaojun.zfile.service.SystemConfigService;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import im.zhaojun.zfile.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理后台接口
 * @author zhaojun
 */
@RestController
@RequestMapping("/admin")
@Slf4j
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
    public ResultBean updateConfig(SystemConfigDTO systemConfigDTO) throws Exception {
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


    /**
     * 获取指定存储策略的表单域
     *
     * @param   storageType
     *          存储策略
     *
     * @return  所有表单域
     */
    @GetMapping("/strategy-form")
    public ResultBean getFormByStorageType(StorageTypeEnum storageType) {
        AbstractBaseFileService storageTypeService = StorageTypeContext.getStorageTypeService(storageType);
        List<StorageConfig> storageConfigList = storageTypeService.storageStrategyConfigList();
        return ResultBean.success(storageConfigList);
    }


    /**
     * 返回支持的存储引擎.
     */
    @GetMapping("/support-strategy")
    public ResultBean supportStrategy() {
        List<StorageStrategyDTO> result = new ArrayList<>();
        StorageTypeEnum[] values = StorageTypeEnum.values();
        return ResultBean.successData(values);
    }


    /**
     * 系统日志下载
     */
    @GetMapping("/log")
    public ResponseEntity<Object> downloadLog(HttpServletResponse response) {
        String userHome = System.getProperty("user.home");
        File fileZip = ZipUtil.zip(userHome + "/.zfile/logs");
        String currentDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        return FileUtil.export(fileZip, "ZFile 诊断日志 - " + currentDate + ".zip");
    }


    /**
     * 获取系统监控信息
     */
    @GetMapping("monitor")
    public ResultBean monitor() {
        return ResultBean.success(new SystemMonitorInfo());
    }

}