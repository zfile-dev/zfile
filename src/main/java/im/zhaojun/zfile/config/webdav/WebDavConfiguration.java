package im.zhaojun.zfile.config.webdav;

import im.zhaojun.zfile.model.constant.ZFileConstant;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.servlet.MiltonFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebDav配置
 *
 * @author me
 * @date 2022/4/9
 */
@Configuration
public class WebDavConfiguration {

    @Bean
    public FilterRegistrationBean miltonFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new MiltonFilter());
        registration.setName("miltonFilter");
        registration.addUrlPatterns(ZFileConstant.WEB_DAV_PREFIX + "/*");
        registration.addInitParameter("resource.factory.class", AnnotationResourceFactory.class.getName());
        registration.addInitParameter("milton.configurator", MiltonConfiguration.class.getName());
        registration.addInitParameter("controllerPackagesToScan", "im.zhaojun.zfile.controller.home");
        registration.setOrder(1);
        return registration;
    }


}
