package im.zhaojun.zfile.module.config.event;

import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.link.controller.DirectLinkController;
import im.zhaojun.zfile.module.link.service.DynamicDirectLinkPrefixService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

/**
 * 接收系统设置修改事件, 修改直链前缀时, 动态更新直链前缀.
 *
 * @author zhaojun
 */
@Slf4j
@Component
public class DirectLinkPrefixModifyHandler implements ISystemConfigModifyHandler {

    @Resource
    private DynamicDirectLinkPrefixService dynamicDirectLinkPrefixService;

    @Override
    public void modify(SystemConfig originalSystemConfig, SystemConfig newSystemConfig) {
        String oldValue = originalSystemConfig.getValue();
        String newValue = newSystemConfig.getValue();
        if (StringUtils.equals(oldValue, newValue)) {
            log.info("检测到修改了直链前缀, 但是新值和旧值相同, 不做处理.");
            return;
        }

        RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(newValue + DirectLinkController.DIRECT_LINK_SUFFIX_PATH).build();
        dynamicDirectLinkPrefixService.updateRegisterMappingHandler(SystemConfig.DIRECT_LINK_PREFIX_NAME, requestMappingInfo);
        log.info("检测到修改了直链前缀, [{}] -> [{}], 已自动更新直链前缀.", oldValue, newValue);
    }

    @Override
    public boolean matches(String name) {
        return SystemConfig.DIRECT_LINK_PREFIX_NAME.equals(name);
    }

}
