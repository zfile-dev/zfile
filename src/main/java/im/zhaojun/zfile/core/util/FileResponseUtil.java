package im.zhaojun.zfile.core.util;

import cn.hutool.core.io.FileUtil;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 将文件输出对象
 *
 * @author zhaojun
 */
@Slf4j
public class FileResponseUtil {


    /**
     * 文件下载，单线程，不支持断点续传
     *
     * @param   file
     *          文件对象
     *
     * @param   fileName
     *          要保存为的文件名
     *
     * @return  文件下载对象
     */
    public static ResponseEntity<Resource> exportSingleThread(File file, String fileName) {
        if (!file.exists()) {
            throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        HttpHeaders headers = new HttpHeaders();

        if (StringUtils.isEmpty(fileName)) {
            fileName = file.getName();
        }

        ContentDisposition contentDisposition = ContentDisposition
                .builder("inline")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(new InputStreamResource(FileUtil.getInputStream(file)));
    }

}