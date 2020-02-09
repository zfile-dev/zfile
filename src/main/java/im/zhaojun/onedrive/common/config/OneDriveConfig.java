package im.zhaojun.onedrive.common.config;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.onedrive.china.service.OneDriveChinaServiceImpl;
import im.zhaojun.onedrive.international.service.OneDriveServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author zhaojun
 */
@Configuration
public class OneDriveConfig {

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    @Lazy
    private OneDriveServiceImpl oneDriveServiceImpl;

    @Resource
    @Lazy
    private OneDriveChinaServiceImpl oneDriveChinaServiceImpl;

    @Bean
    public RestTemplate oneDriveRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor interceptor = (httpRequest, bytes, clientHttpRequestExecution) -> {
            String host = httpRequest.getURI().getHost();
            StorageTypeEnum type;
            if (oneDriveChinaServiceImpl.getGraphEndPoint().contains(host)) {
                type = StorageTypeEnum.ONE_DRIVE_CHINA;
            } else if  (oneDriveServiceImpl.getGraphEndPoint().contains(host)) {
                type = StorageTypeEnum.ONE_DRIVE;
            } else {
                return clientHttpRequestExecution.execute(httpRequest, bytes);
            }

            StorageConfig accessTokenConfig =
                    storageConfigService.selectByTypeAndKey(type, StorageConfigConstant.ACCESS_TOKEN_KEY);

            String tokenValue = String.format("%s %s", "Bearer", accessTokenConfig.getValue());
            httpRequest.getHeaders().add("Authorization", tokenValue);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }

}
