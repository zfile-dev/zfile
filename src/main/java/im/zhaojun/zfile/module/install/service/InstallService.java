package im.zhaojun.zfile.module.install.service;

import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.module.config.model.dto.SystemConfigDTO;
import im.zhaojun.zfile.module.install.model.request.InstallSystemRequest;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class InstallService {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UserService userService;

    @Transactional(rollbackFor = Exception.class)
    public void install(InstallSystemRequest installSystemRequest) {
        if (getSystemIsInstalled()) {
            throw new BizException(ErrorCode.BIZ_SYSTEM_ALREADY_INIT);
        }

        boolean updateFlag = userService.initAdminUser(installSystemRequest.getUsername(),
                installSystemRequest.getPassword());
        if (!updateFlag) {
            throw new SystemException(ErrorCode.BIZ_SYSTEM_INIT_ERROR);
        }

        SystemConfigDTO systemConfigDTO = new SystemConfigDTO();
        systemConfigDTO.setSiteName(installSystemRequest.getSiteName());
        systemConfigDTO.setInstalled(true);
        systemConfigService.updateSystemConfig(systemConfigDTO);
    }

    /**
     * 获取系统是否已初始化
     *
     * @return  管理员名称
     */
    public Boolean getSystemIsInstalled() {
        return systemConfigService.getSystemConfig().getInstalled();
    }

}