package im.zhaojun.onedrive.config;

import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.model.constant.StorageConfigConstant;
import im.zhaojun.common.model.enums.StorageTypeEnum;
import im.zhaojun.common.service.StorageConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author zhaojun
 * @date 2020/1/18 17:13
 */
@Configuration
public class OneDriveConfig {

    @Resource
    private StorageConfigService storageConfigService;

    @Bean
    public RestTemplate oneDriveRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor interceptor = (httpRequest, bytes, clientHttpRequestExecution) -> {
            StorageConfig accessTokenConfig =
                    storageConfigService.selectByTypeAndKey(StorageTypeEnum.ONE_DRIVE, StorageConfigConstant.ACCESS_TOKEN_KEY);
            String tokenValue = String.format("%s %s", "Bearer", accessTokenConfig.getValue());
            httpRequest.getHeaders().add("Authorization", tokenValue);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }


}
