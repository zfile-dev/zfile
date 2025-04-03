package im.zhaojun.zfile.module.link.service;

import im.zhaojun.zfile.module.link.dto.DynamicRegisterMappingHandlerDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态请求映射服务，用于在线注册、修改、注销 @RequestMapping 注解的方法.
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class DynamicDirectLinkPrefixService {

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public static final Map<String, DynamicRegisterMappingHandlerDTO> REGISTER_MAPPING = new ConcurrentHashMap<>();

    public void registerMappingHandlerMapping(String key, RequestMappingInfo requestMappingInfo, Object controllerObj, Method directLinkMethod) {
        requestMappingHandlerMapping.registerMapping(requestMappingInfo, controllerObj, directLinkMethod);
        REGISTER_MAPPING.put(key, new DynamicRegisterMappingHandlerDTO(requestMappingInfo, controllerObj, directLinkMethod));

    }

    public void updateRegisterMappingHandler(String key, RequestMappingInfo requestMappingInfo) {
        synchronized (key.intern()) {
            DynamicRegisterMappingHandlerDTO dynamicRegisterMappingHandlerDTO = REGISTER_MAPPING.get(key);
            if (dynamicRegisterMappingHandlerDTO != null) {
                requestMappingHandlerMapping.unregisterMapping(dynamicRegisterMappingHandlerDTO.getRequestMappingInfo());
                requestMappingHandlerMapping.registerMapping(requestMappingInfo, dynamicRegisterMappingHandlerDTO.getObject(), dynamicRegisterMappingHandlerDTO.getMethod());
                REGISTER_MAPPING.put(key, new DynamicRegisterMappingHandlerDTO(requestMappingInfo, dynamicRegisterMappingHandlerDTO.getObject(), dynamicRegisterMappingHandlerDTO.getMethod()));
            }
        }
    }

}