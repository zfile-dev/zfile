package im.zhaojun.common.service;

import im.zhaojun.common.model.SystemMonitorInfo;
import org.springframework.stereotype.Service;

/**
 * @author zhaojun
 */
@Service
public class SystemMonitorService {

    public SystemMonitorInfo systemMonitorInfo() {
        return new SystemMonitorInfo();
    }
}
