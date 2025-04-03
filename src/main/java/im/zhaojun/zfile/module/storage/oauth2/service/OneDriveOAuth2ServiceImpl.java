package im.zhaojun.zfile.module.storage.oauth2.service;

import im.zhaojun.zfile.core.config.ZFileProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class OneDriveOAuth2ServiceImpl extends AbstractMicrosoftOAuth2Service {

    @Resource
    private ZFileProperties zFileProperties;

    @Override
    public String getEndPoint() {
        return "login.microsoftonline.com";
    }

    @Override
    public String getClientId() {
        return zFileProperties.getOnedrive().getClientId();
    }

    @Override
    public String getClientSecret() {
        return zFileProperties.getOnedrive().getClientSecret();
    }

    @Override
    public String getRedirectUri() {
        return zFileProperties.getOnedrive().getRedirectUri();
    }

    @Override
    public String getScope() {
        return zFileProperties.getOnedrive().getScope();
    }
}
