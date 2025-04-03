package im.zhaojun.zfile.module.config.event;

import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author zhaojun
 */
@Component
public class SystemConfigModifyHandlerChain {

    @Resource
    private List<ISystemConfigModifyHandler> handlers;

    public void execute(SystemConfig originalSystemConfig, SystemConfig newSystemConfig) {
        handlers.stream()
                .filter(handler -> handler.matches(originalSystemConfig.getName()))
                .forEach(handler -> handler.modify(originalSystemConfig, newSystemConfig));
    }

}