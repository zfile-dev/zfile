package im.zhaojun.zfile.config;

import im.zhaojun.zfile.filter.CorsFilter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * @author zhaojun
 */
@Configuration
public class ZFileConfiguration {

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