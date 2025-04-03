package im.zhaojun.zfile.module.storage.oauth2.service;

import im.zhaojun.zfile.core.config.ZFileProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class OneDriveChinaOAuth2ServiceImpl extends AbstractMicrosoftOAuth2Service {

    @Resource
    private ZFileProperties zFileProperties;

    @Override
    public String getEndPoint() {
        return "login.partner.microsoftonline.cn";
    }

    @Override
    public String getClientId() {
        return zFileProperties.getOnedriveChina().getClientId();
    }

    @Override
    public String getClientSecret() {
        return zFileProperties.getOnedriveChina().getClientSecret();
    }

    @Override
    public String getRedirectUri() {
        return zFileProperties.getOnedriveChina().getRedirectUri();
    }

    @Override
    public String getScope() {
        return zFileProperties.getOnedriveChina().getScope();
    }
}
