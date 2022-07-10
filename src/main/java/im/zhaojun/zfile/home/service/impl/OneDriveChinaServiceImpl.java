package im.zhaojun.zfile.home.service.impl;

import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.admin.model.param.OneDriveChinaParam;
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
public class OneDriveChinaServiceImpl extends AbstractOneDriveServiceBase<OneDriveChinaParam> {

    @Value("${zfile.onedrive-china.clientId}")
    private String clientId;

    @Value("${zfile.onedrive-china.redirectUri}")
    private String redirectUri;

    @Value("${zfile.onedrive-china.clientSecret}")
    private String clientSecret;

    @Value("${zfile.onedrive-china.scope}")
    private String scope;

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.ONE_DRIVE_CHINA;
    }

    @Override
    public String getGraphEndPoint() {
        return "microsoftgraph.chinacloudapi.cn";
    }

    @Override
    public String getAuthenticateEndPoint() {
        return "login.partner.microsoftonline.cn";
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getScope() {
        return scope;
    }

}