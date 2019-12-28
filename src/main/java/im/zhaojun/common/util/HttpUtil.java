package im.zhaojun.common.util;

import org.springframework.web.client.RestTemplate;

/**
 * @author zhaojun
 */
public class HttpUtil {

    public static String getTextContent(String url) {
        RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
        String result = restTemplate.getForObject(url, String.class);
        return result == null ? "" : result;
    }

}
