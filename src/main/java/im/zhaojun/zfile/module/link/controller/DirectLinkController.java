package im.zhaojun.zfile.module.link.controller;

import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.config.model.entity.SystemConfig;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.core.util.SpringMvcUtils;
import im.zhaojun.zfile.module.link.service.DynamicDirectLinkPrefixService;
import im.zhaojun.zfile.module.link.service.LinkDownloadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 短链接口
 *
 * @author zhaojun
 */
@Tag(name = "短链")
@ApiSort(5)
@Controller
@Slf4j
public class DirectLinkController {

    @Resource
    private LinkDownloadService linkDownloadService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DynamicDirectLinkPrefixService dynamicDirectLinkPrefixService;

    public static final String DIRECT_LINK_SUFFIX_PATH = "/{storageKey}/**";

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws NoSuchMethodException {
        String directLinkPrefix = systemConfigService.getSystemConfig().getDirectLinkPrefix();
        Method directLinkMethod = DirectLinkController.class.getMethod("directLink", String.class);
        RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(directLinkPrefix + DIRECT_LINK_SUFFIX_PATH).build();
        dynamicDirectLinkPrefixService.registerMappingHandlerMapping(SystemConfig.DIRECT_LINK_PREFIX_NAME, requestMappingInfo, this, directLinkMethod);
    }

    /**
     * 路径直链处理方法，会根据 URL 中的存储源 key 和文件路径, 获取到文件，判断文件是否有短链，没有则生成，然后跳转到短链.
     *
     * @param   storageKey
     *          存储源 key
     */
    public ResponseEntity<?> directLink(@PathVariable("storageKey") String storageKey) throws IOException {
        // 获取直链全路径
        String filePath = SpringMvcUtils.getExtractPathWithinPattern();

        // 如果路径不是以 / 开头, 则补充上
        if (StringUtils.isNotEmpty(filePath) && filePath.charAt(0) != StringUtils.SLASH_CHAR) {
            filePath = StringUtils.SLASH + filePath;
        }

        return linkDownloadService.handlerDirectLink(storageKey, filePath);
    }

}