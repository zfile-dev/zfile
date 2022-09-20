package im.zhaojun.zfile.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
            ByteArrayResource byteArrayResource = new ByteArrayResource("文件不存在或异常，请联系管理员.".getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(byteArrayResource);
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        HttpHeaders headers = new HttpHeaders();

        if (StrUtil.isEmpty(fileName)) {
            fileName = file.getName();
        }

        headers.setContentDispositionFormData("attachment", StringUtils.encodeAllIgnoreSlashes(fileName));

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(new InputStreamResource(FileUtil.getInputStream(file)));
    }

}