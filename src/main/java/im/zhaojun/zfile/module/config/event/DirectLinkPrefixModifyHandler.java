package im.zhaojun.zfile.module.config.event;

import im.zhaojun.zfile.core.service.DynamicControllerManager;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhaojun
 */
@Slf4j
@Component
public class DirectLinkPrefixModifyHandler implements ISystemConfigModifyHandler {

    @Resource
    private DynamicControllerManager dynamicControllerManager;

    @Override
    public void modify(SystemConfig originalSystemConfig, SystemConfig newSystemConfig) {
        dynamicControllerManager.changeDirectLinkPrefixPath(newSystemConfig.getValue());
        log.info("检测到修改了直链前缀, [{}] -> [{}], 已自动更新直链前缀.", originalSystemConfig.getValue(), newSystemConfig.getValue());
    }

    @Override
    public boolean matches(String name) {
        return SystemConfig.DIRECT_LINK_PREFIX_NAME.equals(name);
    }

}
