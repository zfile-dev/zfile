package im.zhaojun.zfile.config;

import im.zhaojun.zfile.model.constant.StorageConfigConstant;
import im.zhaojun.zfile.model.entity.StorageConfig;
import im.zhaojun.zfile.service.StorageConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author zhaojun
 */
@Configuration
public class OneDriveConfig {

    @Resource
    private StorageConfigService storageConfigService;

    /**
     * OneDrive 请求 RestTemplate, 会在请求头中添加 Bearer: Authorization {token} 信息, 用于 API 认证.
     */
    @Bean
    public RestTemplate oneDriveRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor interceptor = (httpRequest, bytes, clientHttpRequestExecution) -> {
            HttpHeaders headers = httpRequest.getHeaders();
            Integer driveId = Integer.valueOf(((LinkedList)headers.get("driveId")).get(0).toString());

            StorageConfig accessTokenConfig =
                    storageConfigService.findByDriveIdAndKey(driveId, StorageConfigConstant.ACCESS_TOKEN_KEY);

            String tokenValue = String.format("%s %s", "Bearer", accessTokenConfig.getValue());
            httpRequest.getHeaders().add("Authorization", tokenValue);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }

}