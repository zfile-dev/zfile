package im.zhaojun.zfile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
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
        RestTemplate restTemplate = new RestTemplate();
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