package im.zhaojun.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

public class ContentTypeTextToTextJson implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ContentTypeTextToTextJson.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        URI uri = request.getURI();
        ClientHttpResponse response = execution.execute(request, body);
        HttpHeaders headers = response.getHeaders();
        headers.put("Content-Type", Collections.singletonList("application/text"));
        return response;
    }

} 