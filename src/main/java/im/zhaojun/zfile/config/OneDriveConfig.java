package im.zhaojun.zfile.config;

import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.service.impl.OneDriveChinaServiceImpl;
import im.zhaojun.zfile.service.impl.OneDriveServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private OneDriveServiceImpl oneDriveServiceImpl;

    @Resource
    private OneDriveChinaServiceImpl oneDriveChinaServiceImpl;


    /**
     * OneDrive 请求 RestTemplate, 会在请求头中添加 Bearer: Authorization {token} 信息, 用于 API 认证.
     */
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