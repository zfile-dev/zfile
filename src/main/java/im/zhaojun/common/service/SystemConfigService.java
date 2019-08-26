package im.zhaojun.common.service;

import im.zhaojun.common.model.SystemConfig;
import im.zhaojun.common.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SystemConfigService {

    @Resource
    private SystemConfigRepository systemConfigRepository;

    public SystemConfig getSystemConfig() {
        return systemConfigRepository.findFirstBy();
    }
}
