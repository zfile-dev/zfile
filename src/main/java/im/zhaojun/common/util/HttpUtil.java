package im.zhaojun.common.util;

import cn.hutool.core.util.URLUtil;
import org.springframework.web.client.RestTemplate;

public class HttpUtil {

    public static String getTextContent(String url) {
        RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
        String result = restTemplate.getForObject(URLUtil.decode(url), String.class);
        return result == null ? "" : result;
    }

}
