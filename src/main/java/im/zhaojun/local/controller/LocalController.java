package im.zhaojun.local.controller;

import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.util.StringUtils;
import im.zhaojun.local.service.LocalService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

@Controller
public class LocalController {

    @Resource
    private LocalService localService;

    @GetMapping("/local-download")
    @ResponseBody
    public ResponseEntity<FileSystemResource> downAttachment(String fileName) throws IOException {
        return export(new File(StringUtils.concatDomainAndPath(localService.getFilePath(), URLUtil.decode(fileName))));
    }

    private ResponseEntity<FileSystemResource> export(File file) throws IOException {
        // 获取文件 MIME 类型
        String fileMimeType = Files.probeContentType(file.toPath());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        HttpHeaders headers = new HttpHeaders();

        // 如果
        if (fileMimeType == null || "".equals(fileMimeType)) {
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        } else {
            mediaType = MediaType.parseMediaType(fileMimeType);
        }
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
