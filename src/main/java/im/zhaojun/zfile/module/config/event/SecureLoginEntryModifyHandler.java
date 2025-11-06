package im.zhaojun.zfile.module.config.event;

import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.user.service.DynamicLoginEntryService;
import im.zhaojun.zfile.module.user.util.LoginEntryPathUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * 监听安全登录入口配置变更，动态更新登录接口映射。
 *
 * @author zhaojun
 */
@Slf4j
@Component
public class SecureLoginEntryModifyHandler implements ISystemConfigModifyHandler {

    @Resource
    private DynamicLoginEntryService dynamicLoginEntryService;

    @Override
    public void modify(SystemConfig originalSystemConfig, SystemConfig newSystemConfig) {
        String oldPath = LoginEntryPathUtils.resolveLoginPath(originalSystemConfig.getValue());
        String newPath = LoginEntryPathUtils.resolveLoginPath(newSystemConfig.getValue());

        if (StringUtils.equals(oldPath, newPath)) {
            log.info("检测到修改安全登录入口，但实际登录路径未变化，跳过处理。");
            return;
        }

        RequestMappingInfo requestMappingInfo = dynamicLoginEntryService.buildLoginRequestMappingInfo(newSystemConfig.getValue());
        dynamicLoginEntryService.updateRegisterMappingHandler(SystemConfig.SECURE_LOGIN_ENTRY_NAME, requestMappingInfo);
        log.info("安全登录入口已更新，{} -> {}", oldPath, newPath);
    }

    @Override
    public boolean matches(String name) {
        return SystemConfig.SECURE_LOGIN_ENTRY_NAME.equals(name);
    }

}