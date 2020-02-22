package im.zhaojun.onedrive.international.service;

import im.zhaojun.common.config.GlobalScheduleTask;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.onedrive.common.service.AbstractOneDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author zhaojun
 */
@Service
@Slf4j
public class OneDriveServiceImpl extends AbstractOneDriveService implements FileService {

    @Resource
    private GlobalScheduleTask globalScheduleTask;

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
    public void init() {
        try {
            Map<String, StorageConfig> stringStorageConfigMap =
                    storageConfigService.selectStorageConfigMapByKey(getStorageTypeEnum());
            String accessToken = stringStorageConfigMap.get(StorageConfigConstant.ACCESS_TOKEN_KEY).getValue();
            String refreshToken = stringStorageConfigMap.get(StorageConfigConstant.REFRESH_TOKEN_KEY).getValue();
            super.basePath = stringStorageConfigMap.get(StorageConfigConstant.BASE_PATH).getValue();

            if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
                log.debug("初始化存储策略 [{}] 失败: 参数不完整", getStorageTypeEnum().getDescription());
                isInitialized = false;
            } else {
                refreshOneDriveToken();
                isInitialized = testConnection();
            }
        } catch (Exception e) {
            log.debug(getStorageTypeEnum().getDescription() + " 初始化异常, 已跳过");
        }
    }

    @Override
    public String getDownloadUrl(String path) {
        return null;
    }

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