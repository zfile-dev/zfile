package im.zhaojun.zfile.module.link.controller;

import com.github.xiaoymin.knife4j.annotations.ApiSort;
import im.zhaojun.zfile.core.constant.ZFileConstant;
import im.zhaojun.zfile.core.service.DynamicControllerManager;
import im.zhaojun.zfile.module.config.service.SystemConfigService;
import im.zhaojun.zfile.module.config.utils.SpringMvcUtils;
import im.zhaojun.zfile.module.link.service.LinkDownloadService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 短链接口
 *
 * @author zhaojun
 */
@Api(tags = "短链")
@ApiSort(5)
@Controller
@Slf4j
public class DirectLinkController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private DynamicControllerManager dynamicControllerManager;

    @Resource
    private LinkDownloadService linkDownloadService;

    @PostConstruct
    public void init() throws NoSuchMethodException {
        String directLinkPrefix = systemConfigService.getSystemConfig().getDirectLinkPrefix();
        Method directLinkMethod = DirectLinkController.class.getMethod("directLink", String.class);
        dynamicControllerManager.initDirectLinkPrefixPath(directLinkPrefix, this, directLinkMethod);
    }

    /**
     * 路径直链处理方法，会根据 URL 中的存储源 key 和文件路径, 获取到文件，判断文件是否有短链，没有则生成，然后跳转到短链.
     *
     * @param   storageKey
     *          存储源 key
     */
    @ResponseBody
    public void directLink(@PathVariable("storageKey") String storageKey) throws IOException {
        // 获取直链全路径
        String filePath = SpringMvcUtils.getExtractPathWithinPattern();

        // 如果路径不是以 / 开头, 则补充上
        if (filePath.length() > 0 && filePath.charAt(0) != ZFileConstant.PATH_SEPARATOR_CHAR) {
            filePath = "/" + filePath;
        }

        linkDownloadService.handlerDirectLink(storageKey, filePath);
    }

}