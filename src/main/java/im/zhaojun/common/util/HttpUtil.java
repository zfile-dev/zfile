package im.zhaojun.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author zhaojun
 */
@Slf4j
public class HttpUtil {

    public static String getTextContent(String url) {
        RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
        String result = restTemplate.getForObject(url, String.class);
        return result == null ? "" : result;
    }

    public static boolean checkUrlExist(String url) {
        RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
        try {
            restTemplate.headForHeaders(url);
            return true;
        } catch (RestClientException ignored) {
        }
        return false;
    }

}
