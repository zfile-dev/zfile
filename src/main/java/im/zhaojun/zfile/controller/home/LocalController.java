package im.zhaojun.zfile.controller.home;

import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.service.impl.LocalServiceImpl;
import im.zhaojun.zfile.util.FileUtil;
import im.zhaojun.zfile.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * 本地存储 Controller
 * @author zhaojun
 */
@Controller
public class LocalController {

    @Resource
    private DriveContext driveContext;

    /**
     * 本地存储下载指定文件
     *
     * @param   driveId
     *          驱动器 ID
     */
    @GetMapping("/file/{driveId}/**")
    @ResponseBody
    public void downAttachment(@PathVariable("driveId") Integer driveId, final HttpServletRequest request, final HttpServletResponse response) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);
        LocalServiceImpl localService = (LocalServiceImpl) driveContext.get(driveId);
        File file = new File(StringUtils.removeDuplicateSeparator(localService.getFilePath() + ZFileConstant.PATH_SEPARATOR + filePath));
        FileUtil.export(request, response, file);
    }

}