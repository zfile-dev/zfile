package im.zhaojun.zfile.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.model.constant.LocalFileResponseTypeConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Objects;

/**
 * @author zhaojun
 */
@Slf4j
public class FileUtil {

    /**
     * 文件下载，单线程，直接传
     * @param file          文件对象
     * @param fileName      要保存为的文件名
     * @return              文件下载对象
     */
    public static ResponseEntity<Object> exportSingleThread(File file, String fileName) {
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404 FILE NOT FOUND");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        if (StringUtils.isNullOrEmpty(fileName)) {
            fileName = file.getName();
        }

        headers.setContentDispositionFormData("attachment", URLUtil.encode(fileName));

        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        headers.add(HttpHeaders.LAST_MODIFIED, new Date().toString());
        headers.add(HttpHeaders.ETAG, String.valueOf(System.currentTimeMillis()));
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }

    /**
     * 返回文件给 response，支持断点续传和多线程下载
     * @param request       请求对象
     * @param response      响应对象
     * @param file          下载的文件
     */
    public static void export(HttpServletRequest request, HttpServletResponse response, File file, String type) {
        export(request, response, file, file.getName(), type);
    }

    /**
     * 返回文件给 response，支持断点续传和多线程下载 (动态变化的文件不支持)
     * @param request       请求对象
     * @param response      响应对象
     * @param file          下载的文件
     * @param fileName      下载的文件名，为空则默认读取文件名称
     */
    public static void export(HttpServletRequest request, HttpServletResponse response, File file, String fileName, String type) {
        if (!file.exists()) {
            try {
                response.getWriter().write("404 FILE NOT FOUND");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (StringUtils.isNullOrEmpty(fileName)) {
            //文件名
            fileName = file.getName();
        }

        String range = request.getHeader(HttpHeaders.RANGE);

        String rangeSeparator = "-";
        // 开始下载位置
        long startByte = 0;
        // 结束下载位置
        long endByte = file.length() - 1;

        // 如果是断点续传
        if (range != null && range.contains("bytes=") && range.contains(rangeSeparator)) {
            // 设置响应状态码为 206
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split(rangeSeparator);
            try {
                // 判断 range 的类型
                if (ranges.length == 1) {
                    // 类型一：bytes=-2343
                    if (range.startsWith(rangeSeparator)) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    // 类型二：bytes=2343-
                    else if (range.endsWith(rangeSeparator)) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                // 类型三：bytes=22-2343
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                // 传参不规范，则直接返回所有内容
                startByte = 0;
                endByte = file.length() - 1;
            }
        } else {
            // 没有 ranges 即全部一次性传输，需要用 200 状态码，这一行应该可以省掉，因为默认返回是 200 状态码
            response.setStatus(HttpServletResponse.SC_OK);
        }

        //要下载的长度（endByte 为总长度 -1，这时候要加回去）
        long contentLength = endByte - startByte + 1;
        //文件类型
        String contentType = request.getServletContext().getMimeType(fileName);
        if (StrUtil.isEmpty(contentType)) {
            contentType = "application/octet-stream";
        }

        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        // 这里文件名换你想要的，inline 表示浏览器可以直接使用
        // 参考资料：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Content-Disposition
        if (Objects.equals(type, LocalFileResponseTypeConstant.DOWNLOAD) || StrUtil.isEmpty(contentType)) {
            String contentDisposition = "attachment;filename=" + URLUtil.encode(fileName);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,  contentDisposition);
        }
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        // [要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + startByte + rangeSeparator + endByte + "/" + file.length());

        BufferedOutputStream outputStream;
        RandomAccessFile randomAccessFile = null;
        //已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[4096];
            int len = 0;
            randomAccessFile.seek(startByte);
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
                // 本地测试, 防止下载速度过快
                // Thread.sleep(1);
            }
            // 处理不足 buff.length 部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }

            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
            // log.trace("下载完毕: {}-{}, 已传输 {}", startByte, endByte, transmitted);
        } catch (ClientAbortException e) {
            // ignore 用户停止下载
            // log.trace("用户停止下载: {}-{}, 已传输 {}", startByte, endByte, transmitted);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}