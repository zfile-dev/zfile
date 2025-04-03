package im.zhaojun.zfile.module.storage.service.impl;

import im.zhaojun.zfile.core.config.ZFileProperties;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.OneDriveParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractOneDriveServiceBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OneDriveServiceImpl extends AbstractOneDriveServiceBase<OneDriveParam> {

    @Resource
    private ZFileProperties zFileProperties;
    
    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.ONE_DRIVE;
    }

    @Override
    public String getGraphEndPoint() {
        return "graph.microsoft.com";
    }

    @Override
    public String getAuthenticateEndPoint() {
        return "login.microsoftonline.com";
    }
    
    @Override
    public String getClientId() {
        if (param == null || param.getClientId() == null) {
            return zFileProperties.getOnedrive().getClientId();
        }
        return param.getClientId();
    }
    
    @Override
    public String getRedirectUri() {
        if (param == null || param.getRedirectUri() == null) {
            return zFileProperties.getOnedrive().getRedirectUri();
        }
        return param.getRedirectUri();
    }
    
    @Override
    public String getClientSecret() {
        if (param == null || param.getClientSecret() == null) {
            return zFileProperties.getOnedrive().getClientSecret();
        }
        return param.getClientSecret();
    }

    @Override
    public String getScope() {
        return zFileProperties.getOnedrive().getScope();
    }

}