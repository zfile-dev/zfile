package im.zhaojun.common.config;

import im.zhaojun.common.model.enums.StorageTypeEnumDeSerializerConvert;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhaojun
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StorageTypeEnumDeSerializerConvert());
    }

    @Bean
    public ServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory webServerFactory = new TomcatServletWebServerFactory();
        webServerFactory.addConnectorCustomizers(connector -> {
            connector.setAttribute("relaxedPathChars", "<>[\\]^`{|}");
            connector.setAttribute("relaxedQueryChars", "<>[\\]^`{|}");
        });
        return webServerFactory;
    }
}