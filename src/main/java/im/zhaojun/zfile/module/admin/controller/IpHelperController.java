package im.zhaojun.zfile.module.admin.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import im.zhaojun.zfile.core.util.AjaxJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public AjaxJson<String> clientIp() {
        String clientIp = JakartaServletUtil.getClientIP(httpServletRequest);
        return AjaxJson.getSuccessData(clientIp);
    }

}