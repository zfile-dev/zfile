package im.zhaojun.zfile.module.user.service;

import im.zhaojun.zfile.module.link.dto.DynamicRegisterMappingHandlerDTO;
import im.zhaojun.zfile.module.user.util.LoginEntryPathUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.http.MediaType;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态登录入口注册服务，负责动态注册或更新登录接口的请求映射。
 *
 * @author zhaojun
 */
@Slf4j
@Service
public class DynamicLoginEntryService {

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private static final Map<String, DynamicRegisterMappingHandlerDTO> REGISTER_MAPPING = new ConcurrentHashMap<>();

    public void registerMappingHandlerMapping(String key, RequestMappingInfo requestMappingInfo, Object controllerObj, Method method) {
        requestMappingHandlerMapping.registerMapping(requestMappingInfo, controllerObj, method);
        REGISTER_MAPPING.put(key, new DynamicRegisterMappingHandlerDTO(requestMappingInfo, controllerObj, method));
    }

    public void updateRegisterMappingHandler(String key, RequestMappingInfo requestMappingInfo) {
        synchronized (key.intern()) {
            DynamicRegisterMappingHandlerDTO dynamicRegisterMappingHandlerDTO = REGISTER_MAPPING.get(key);
            if (dynamicRegisterMappingHandlerDTO != null) {
                requestMappingHandlerMapping.unregisterMapping(dynamicRegisterMappingHandlerDTO.getRequestMappingInfo());
                requestMappingHandlerMapping.registerMapping(requestMappingInfo, dynamicRegisterMappingHandlerDTO.getObject(), dynamicRegisterMappingHandlerDTO.getMethod());
                REGISTER_MAPPING.put(key, new DynamicRegisterMappingHandlerDTO(requestMappingInfo, dynamicRegisterMappingHandlerDTO.getObject(), dynamicRegisterMappingHandlerDTO.getMethod()));
            } else {
                log.warn("尝试更新不存在的动态登录入口映射，key: {}", key);
            }
        }
    }

    public RequestMappingInfo buildLoginRequestMappingInfo(String secureLoginEntry) {
        String loginPath = LoginEntryPathUtils.resolveLoginApiPath(secureLoginEntry);
        return RequestMappingInfo.paths(loginPath)
                .methods(RequestMethod.POST)
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}