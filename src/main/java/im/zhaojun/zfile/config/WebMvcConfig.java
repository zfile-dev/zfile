package im.zhaojun.zfile.config;

import im.zhaojun.zfile.model.enums.StorageTypeEnumDeSerializerConvert;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashSet;
import java.util.Set;

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

        // 添加对 URL 中特殊符号的支持.
        webServerFactory.addConnectorCustomizers(connector -> {
            connector.setAttribute("relaxedPathChars", "<>[\\]^`{|}");
            connector.setAttribute("relaxedQueryChars", "<>[\\]^`{|}");
        });
        return webServerFactory;
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(){
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            ErrorPage error200Page = new ErrorPage(HttpStatus.OK, "/index.html");
            Set<ErrorPage> errorPages = new HashSet<>();
            errorPages.add(error404Page);
            errorPages.add(error200Page);
            factory.setErrorPages(errorPages);
        };
    }

}