package im.zhaojun.common.service;

import im.zhaojun.common.mapper.SystemConfigMapper;
import im.zhaojun.common.model.SystemConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    @Cacheable("zfile")
    public SystemConfig getSystemConfig() {
        return systemConfigMapper.selectFirstConfig();
    }
}
