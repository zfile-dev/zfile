package im.zhaojun.zfile.util;

import im.zhaojun.zfile.exception.PreviewException;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author zhaojun
 */
@Slf4j
public class HttpUtil {

    /**
     * 最大支持文件预览大小: 1M
     */
    public static String getTextContent(String url) {
        RestTemplate restTemplate = SpringContextHolder.getBean("restTemplate");

        if (getRemoteFileSize(url) > (1024 * ZFileConstant.TEXT_MAX_FILE_SIZE_KB)) {
            throw new PreviewException("存储源跨域请求失败, 服务器中转状态, 预览文件超出大小, 最大支持 1M");
        }

        String result = restTemplate.getForObject(url, String.class);
        return result == null ? "" : result;
    }

    /**
     * 获取远程文件大小
     */
    public static Long getRemoteFileSize(String url) {
        long size = 0;
        URL urlObject;
        try {
            urlObject = new URL(url);
            URLConnection conn = urlObject.openConnection();
            size = conn.getContentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return size;
    }

}
