package im.zhaojun.zfile.core.config.spring;

import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ZFile Web 相关配置.
 *
 * @author zhaojun
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 支持 url 中传入 <>[\]^`{|} 这些特殊字符.
     */
    @Bean
    public ServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory webServerFactory = new TomcatServletWebServerFactory();

        // 添加对 URL 中特殊符号的支持.
        webServerFactory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "<>[\\]^`{|}%[]");
            connector.setProperty("relaxedQueryChars", "<>[\\]^`{|}%[]");
        });
        return webServerFactory;
    }

    /**
     * 添加自定义枚举格式化器.
     * @see StorageTypeEnum
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

}