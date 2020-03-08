package im.zhaojun.zfile.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Collections;

/**
 * @author zhaojun
 */
public class ContentTypeTextToTextJson implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        HttpHeaders headers = response.getHeaders();
        headers.put("Content-Type", Collections.singletonList("application/text"));
        return response;
    }

} 