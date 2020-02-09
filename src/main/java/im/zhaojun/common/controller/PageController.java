package im.zhaojun.common.controller;

import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.service.AbstractFileService;
import im.zhaojun.common.service.SystemConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.Objects;

/**
 * @author Zhao Jun
 * 2020/2/9 11:17
 */
@Controller
public class PageController {

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping("/directlink/**")
    public String directlink(final HttpServletRequest request) throws MalformedURLException {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);

        if (filePath.length() > 0 && filePath.charAt(0) != '/') {
            filePath = "/" + filePath;
        }

        AbstractFileService fileService = systemConfigService.getCurrentFileService();
        FileItemDTO fileItem = fileService.getFileItem(filePath);

        String url = fileItem.getUrl();

        int queryIndex = url.indexOf('?');

        if (queryIndex != -1) {
            String origin = url.substring(0, queryIndex);
            String queryString = url.substring(queryIndex + 1);

            url = URLUtil.encode(origin) + "?" + URLUtil.encode(queryString);
        } else {
            url = URLUtil.encode(url);
        }


        if (Objects.equals(fileItem.getType(), FileTypeEnum.FOLDER)) {
            return "redirect:" + fileItem.getUrl();
        } else {
            return "redirect:" + url;
        }
    }
}
