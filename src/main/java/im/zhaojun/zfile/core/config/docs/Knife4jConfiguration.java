package im.zhaojun.zfile.core.config.docs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 参数配置，区分前台功能和管理员功能，并为管理员接口增加统一 token header 配置.
 *
 * @author zhaojun
 */
@Configuration
public class Knife4jConfiguration {

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        String groupName = "前台功能";
        return GroupedOpenApi.builder()
                .group(groupName)
                .packagesToScan("im.zhaojun.zfile.module")
                .pathsToExclude("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupedOpenApi2() {
        String groupName = "管理员功能";
        return GroupedOpenApi.builder()
                .group(groupName)
                .packagesToScan("im.zhaojun.zfile.module")
                .pathsToMatch("/admin/**")
                .addOperationCustomizer(globalOperationCustomizer())
                .build();
    }

    public OperationCustomizer globalOperationCustomizer() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new HeaderParameter()
                    .name("zfile-token")
                    .description("token")
                    .required(true)
                    .schema(new StringSchema()));
            return operation;
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("zhaojun");
        contact.setUrl("https://zfile.vip");
        contact.setEmail("873019219@qq.com");

        return new OpenAPI()
                .info(new Info()
                        .title("ZFILE 文档")
                        .description("# 这是 ZFILE Restful 接口文档展示页面")
                        .termsOfService("https://www.zfile.vip")
                        .contact(contact)
                        .version("1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://doc.xiaominfo.com")));
    }

}