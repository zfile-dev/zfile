package im.zhaojun.common.config;

import im.zhaojun.common.model.enums.StorageTypeEnumDeSerializerConvert;
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
}