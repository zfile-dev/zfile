package im.zhaojun.onedrive.international.service;

import im.zhaojun.onedrive.common.service.AbstractOneDriveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author zhaojun
 */
@Service
public class OneDriveService extends AbstractOneDriveService {

    @Value("${zfile.onedirve.clientId}")
    protected String clientId;

    @Value("${zfile.onedirve.redirectUri}")
    protected String redirectUri;

    @Value("${zfile.onedirve.clientSecret}")
    protected String clientSecret;

    @Value("${zfile.onedirve.scope}")
    protected String scope;

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
