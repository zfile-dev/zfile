package im.zhaojun.local.controller;

import im.zhaojun.common.util.FileUtil;
import im.zhaojun.common.util.StringUtils;
import im.zhaojun.local.service.LocalServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author zhaojun
 */
@Controller
public class LocalController {

    @Resource
    private LocalServiceImpl localServiceImpl;

    @GetMapping("/file/**")
    @ResponseBody
    public ResponseEntity<Object> downAttachment(final HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);

        return FileUtil.export(new File(StringUtils.concatPath(localServiceImpl.getFilePath(), filePath)));
    }
}
