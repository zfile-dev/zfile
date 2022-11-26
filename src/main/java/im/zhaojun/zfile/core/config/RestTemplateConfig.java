package im.zhaojun.zfile.core.config;

import im.zhaojun.zfile.core.httpclient.ZFileOkHttp3ClientHttpRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * restTemplate 相关配置
 *
 * @author zhaojun
 */
@Configuration
public class RestTemplateConfig {
	
	/**
	 * OneDrive 请求 RestTemplate.
	 * 获取 header 中的 storageId 来判断到底是哪个存储源 ID, 在请求头中添加 Bearer: Authorization {token} 信息, 用于 API 认证.
	 */
	@Bean
	public RestTemplate oneDriveRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new ZFileOkHttp3ClientHttpRequestFactory());
		return restTemplate;
	}
	
}