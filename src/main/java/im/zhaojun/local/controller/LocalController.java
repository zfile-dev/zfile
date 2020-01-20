package im.zhaojun.local.controller;

import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.exception.NotExistFileException;
import im.zhaojun.common.util.StringUtils;
import im.zhaojun.local.service.LocalServiceImpl;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;

/**
 * @author zhaojun
 */
@Controller
public class LocalController {

    @Resource
    private LocalServiceImpl localServiceImpl;

    @GetMapping("/file/**")
    @ResponseBody
    public ResponseEntity<FileSystemResource> downAttachment(final HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        String filePath = apm.extractPathWithinPattern(bestMatchPattern, path);

        return export(new File(StringUtils.concatPath(localServiceImpl.getFilePath(), URLUtil.decode(filePath))));
    }

    private ResponseEntity<FileSystemResource> export(File file) {

        if (!file.exists()) {
            throw new NotExistFileException();
        }


        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.setContentDispositionFormData("attachment", URLUtil.encode(file.getName()));

        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }

}
