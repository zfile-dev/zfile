package im.zhaojun.zfile.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ZFile 配置类，将配置文件中的 zfile 配置项映射到该类中.
 *
 * @author zhaojun
 */
@Data
@EnableConfigurationProperties
@Component
@ConfigurationProperties(prefix = "zfile")
public class ZFileProperties {

	private boolean debug;

	private String version;

	private boolean isDemoSite;

	private OAuth2Properties onedrive = new OAuth2Properties();
	private OAuth2Properties onedriveChina = new OAuth2Properties();
	private OAuth2Properties gd = new OAuth2Properties();
	private Open115Properties open115 = new Open115Properties();

	@Data
	public static class OAuth2Properties {
		private String clientId;
		private String clientSecret;
		private String redirectUri;
		private String scope;
	}

	@Data
	public static class Open115Properties {
		private String appId;
	}

}