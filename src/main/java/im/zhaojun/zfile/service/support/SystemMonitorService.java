package im.zhaojun.zfile.service.support;

import im.zhaojun.zfile.model.support.SystemMonitorInfo;
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