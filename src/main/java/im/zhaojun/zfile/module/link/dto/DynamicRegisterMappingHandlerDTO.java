package im.zhaojun.zfile.module.link.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
public class DynamicRegisterMappingHandlerDTO {

    private RequestMappingInfo requestMappingInfo;

    private Object object;

    private Method method;

}
