package im.zhaojun.zfile.controller.home;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.context.DriveContext;
import im.zhaojun.zfile.exception.NotAllowedDownloadException;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.service.base.AbstractBaseFileService;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 直链 Controller
 * @author Zhao Jun
 */
@Controller
public class DirectLinkController {

    @Resource
    private DriveContext driveContext;

    /**
     * 获取指定驱动器, 某个文件的直链, 然后重定向过去.
     * @param   driveId
     *          驱动器 ID
     *
     * @return  重定向至文件直链
     */
    @GetMapping("/directlink/{driveId}/**")
    public String directlink(@PathVariable("driveId") Integer driveId,
                             final HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);

        if (filePath.length() > 0 && filePath.charAt(0) != ZFileConstant.PATH_SEPARATOR_CHAR) {
            filePath = "/" + filePath;
        }

        if (Objects.equals(FileUtil.getName(filePath), ZFileConstant.PASSWORD_FILE_NAME)) {
            throw new NotAllowedDownloadException("不允许下载此文件");
        }

        AbstractBaseFileService fileService = driveContext.get(driveId);
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