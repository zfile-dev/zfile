package im.zhaojun.zfile.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhaojun
 * @version V1.0
 * @date 2022-4-26
 */
@Data
@EnableConfigurationProperties
@Component
@ConfigurationProperties(prefix = "zfile")
public class ZFileProperties {

	private boolean debug;

}