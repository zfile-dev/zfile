package im.zhaojun.zfile.service.impl;

import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.service.base.AbstractSharePointServiceBase;
import im.zhaojun.zfile.service.base.BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SharePointServiceImpl extends AbstractSharePointServiceBase implements BaseFileService {

    @Resource
    private StorageConfigService storageConfigService;

    @Value("${zfile.onedrive.clientId}")
    protected String clientId;

    @Value("${zfile.onedrive.redirectUri}")
    protected String redirectUri;

    @Value("${zfile.onedrive.clientSecret}")
    protected String clientSecret;

    @Value("${zfile.onedrive.scope}")
    protected String scope;

    @Override
    public void init(Integer driveId) {
        this.driveId = driveId;
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByDriveId(driveId);
        String accessToken = stringStorageConfigMap.get(StorageConfigConstant.ACCESS_TOKEN_KEY).getValue();
        String refreshToken = stringStorageConfigMap.get(StorageConfigConstant.REFRESH_TOKEN_KEY).getValue();
        super.siteId = stringStorageConfigMap.get(StorageConfigConstant.SHAREPOINT_SITE_ID).getValue();
        super.basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();

        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
            log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
            isInitialized = false;
        } else {
            refreshOneDriveToken();
            testConnection();
            isInitialized = true;
        }
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.SHAREPOINT_DRIVE;
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