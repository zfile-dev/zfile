package im.zhaojun.zfile.core.config;

import im.zhaojun.zfile.module.storage.constant.StorageConfigConstant;
import im.zhaojun.zfile.module.storage.model.entity.StorageSourceConfig;
import im.zhaojun.zfile.module.storage.service.StorageSourceConfigService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * restTemplate 相关配置
 *
 * @author zhaojun
 */
@Configuration
public class RestTemplateConfig {

    @Resource
    private StorageSourceConfigService storageSourceConfigService;

    /**
     * OneDrive 请求 RestTemplate.
     * 获取 header 中的 storageId 来判断到底是哪个存储源 ID, 在请求头中添加 Bearer: Authorization {token} 信息, 用于 API 认证.
     */
    @Bean
    public RestTemplate oneDriveRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory();
        restTemplate.setRequestFactory(factory);
        ClientHttpRequestInterceptor interceptor = (httpRequest, bytes, clientHttpRequestExecution) -> {
            HttpHeaders headers = httpRequest.getHeaders();
            Integer storageId = Integer.valueOf(((List)headers.get("storageId")).get(0).toString());

            StorageSourceConfig accessTokenConfig =
                    storageSourceConfigService.findByStorageIdAndName(storageId, StorageConfigConstant.ACCESS_TOKEN_KEY);

            String tokenValue = String.format("%s %s", "Bearer", accessTokenConfig.getValue());
            httpRequest.getHeaders().add("Authorization", tokenValue);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        };
        restTemplate.setInterceptors(Collections.singletonList(interceptor));
        return restTemplate;
    }


    /**
     * restTemplate 设置请求和响应字符集都为 UTF-8, 并设置响应头为 Content-Type: application/text;
     */
    @Bean
    public RestTemplate restTemplate(){
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpClient = HttpClientBuilder.create().build();
        httpRequestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request, body);
            HttpHeaders headers = response.getHeaders();
            headers.put("Content-Type", Collections.singletonList("application/text"));
            return response;
        }));

        return restTemplate;
    }

}