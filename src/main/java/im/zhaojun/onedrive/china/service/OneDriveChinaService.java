package im.zhaojun.onedrive.china.service;

import im.zhaojun.onedrive.common.service.AbstractOneDriveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author zhaojun
 */
@Service
public class OneDriveChinaService extends AbstractOneDriveService {

    @Value("${zfile.onedirve-china.clientId}")
    private String clientId;

    @Value("${zfile.onedirve-china.redirectUri}")
    private String redirectUri;

    @Value("${zfile.onedirve-china.clientSecret}")
    private String clientSecret;

    @Value("${zfile.onedirve-china.scope}")
    private String scope;

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
