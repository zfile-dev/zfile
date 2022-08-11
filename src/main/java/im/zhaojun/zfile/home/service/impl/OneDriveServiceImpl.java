package im.zhaojun.zfile.home.service.impl;

import cn.hutool.core.util.ObjectUtil;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.admin.model.param.OneDriveParam;
import im.zhaojun.zfile.home.service.base.AbstractOneDriveServiceBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OneDriveServiceImpl extends AbstractOneDriveServiceBase<OneDriveParam> {

    @Value("${zfile.onedrive.clientId}")
    protected String clientId;

    @Value("${zfile.onedrive.redirectUri}")
    protected String redirectUri;

    @Value("${zfile.onedrive.clientSecret}")
    protected String clientSecret;

    @Value("${zfile.onedrive.scope}")
    protected String scope;

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
        return ObjectUtil.defaultIfNull(param.getClientId(), clientId);
    }
    
    @Override
    public String getRedirectUri() {
        return ObjectUtil.defaultIfNull(param.getRedirectUri(), redirectUri);
    }
    
    @Override
    public String getClientSecret() {
        return ObjectUtil.defaultIfNull(param.getClientSecret(), clientSecret);
    }

    @Override
    public String getScope() {
        return scope;
    }

}