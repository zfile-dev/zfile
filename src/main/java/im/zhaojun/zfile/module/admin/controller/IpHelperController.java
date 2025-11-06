package im.zhaojun.zfile.module.admin.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.RequestHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zhaojun
 */
@Tag(name = "IP 地址辅助 Controller")
@Slf4j
@RequestMapping("/admin")
@RestController
public class IpHelperController {

    @Resource
    private HttpServletRequest httpServletRequest;

    @GetMapping("clientIp")
    @Operation(summary = "获取客户端IP", description ="获取当前请求的客户端IP地址")
    public AjaxJson<String> clientIp() {
        String clientIp = JakartaServletUtil.getClientIP(httpServletRequest);
        return AjaxJson.getSuccessData(clientIp);
    }

    @GetMapping("serverAddress")
    @Operation(summary = "获取服务器地址", description = "获取当前请求的服务器地址(如果是反向代理过，可能获取到的是反向代理服务器的地址)")
    public AjaxJson<String> serverAddress() {
        return AjaxJson.getSuccessData(RequestHolder.getRequestServerAddress());
    }

    @GetMapping("headers")
    @Operation(summary = "获取 Headers", description = "获取服务器接收到的请求头信息，可用于排查反向代理配置问题")
    public AjaxJson<String> headers() {
        Map<String, List<String>> headersMap = JakartaServletUtil.getHeadersMap(httpServletRequest);
        Map<String, String> singleValueHeaderMap = headersMap.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));
        return AjaxJson.getSuccessData(JSON.toJSONString(singleValueHeaderMap, JSONWriter.Feature.PrettyFormat));
    }
}