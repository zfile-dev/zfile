package im.zhaojun.zfile.core.util;

import cn.hutool.extra.servlet.JakartaServletUtil;
import im.zhaojun.zfile.core.constant.ZFileHttpHeaderConstant;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 获取 Request 工具类
 *
 * @author zhaojun
 */
@Slf4j
public class RequestHolder {

    /**
     * 获取 HttpServletRequest
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }


    /**
     * 获取 HttpServletResponse
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }


    /**
     * 向 response 写入文件流.
     *
     * @param   inputStream
     *          文件输入流
     *
     * @param   fileName
     *          文件名称
     *
     * @param   fileSize
     *          文件大小
     *
     * @param   isPartialContentFromInputStream
     *          表示输入流是否为部分内容。
     *          当该变量为 true 时，表示输入流已经根据 range 规则从存储源获取部分内容。
     *          在这种情况下，不需要跳过 range start 部分，可以直接从输入流的全部内容复制到输出流。
     *
     * @param   forceDownload
     *          是否强制下载
     */
    public static void writeFile(InputStream inputStream, String fileName, Long fileSize, boolean isPartialContentFromInputStream, boolean forceDownload) {
        if (inputStream == null) {
            throw new BizException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }
        OutputStream outputStream = null;
        try (InputStream innerInputStream = inputStream) {
            HttpServletResponse response = RequestHolder.getResponse();

            if (forceDownload) {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + StringUtils.encodeAllIgnoreSlashes(fileName));
            } else {
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + StringUtils.encodeAllIgnoreSlashes(fileName));
                response.setContentType(MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM).toString());
            }

            outputStream = response.getOutputStream();

            if (fileSize != null && fileSize != 0) {
                String range = RequestHolder.getRequest().getHeader(HttpHeaders.RANGE);
                List<HttpRange> httpRanges = HttpRange.parseRanges(range);
                if (httpRanges.isEmpty()) {
                    httpRanges = Collections.singletonList(HttpRange.createByteRange(0, fileSize - 1));
                } else {
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                }
                HttpRange httpRange = CollectionUtils.getFirst(httpRanges);
                long startPos = httpRange.getRangeStart(fileSize);
                long endPos = httpRange.getRangeEnd(fileSize);
                if (response.getStatus() == HttpServletResponse.SC_PARTIAL_CONTENT) {
                    response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + startPos + "-" + endPos + StringUtils.SLASH + fileSize);
                }

                response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
                response.setContentLengthLong(endPos - startPos + 1);
                if (isPartialContentFromInputStream) {
                    StreamUtils.copy(innerInputStream, outputStream);
                } else {
                    StreamUtils.copyRange(innerInputStream, outputStream, startPos, endPos);
                }
                return;
            }

            StreamUtils.copy(innerInputStream, outputStream);
        } catch (IOException e) {
            boolean isBrokenPipe = e.getMessage().contains("Broken pipe");
            boolean isConnectionResetByPeer = e.getMessage().contains("Connection reset by peer");
            if (isBrokenPipe || isConnectionResetByPeer) {
                if (log.isDebugEnabled()) {
                    log.debug("skip IOException: {}", e.getMessage());
                }
            } else {
                throw new SystemException(e);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    public static boolean isAxiosRequest() {
        HttpServletRequest request = RequestHolder.getRequest();
        String axiosRequest = JakartaServletUtil.getHeaderIgnoreCase(request, ZFileHttpHeaderConstant.AXIOS_REQUEST);
        return StringUtils.isNotEmpty(axiosRequest);
    }


    /**
     * 获取请求头中的 Axios-From 字段
     *
     * @return Axios-From 字段
     */
    public static String getAxiosFrom() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        HttpServletRequest request = RequestHolder.getRequest();
        return JakartaServletUtil.getHeaderIgnoreCase(request, ZFileHttpHeaderConstant.AXIOS_FROM);
    }

    /**
     * 获取后端服务地址，如果经过了反向代理，需反向代理正确配置
     */
    public static String getRequestServerAddress() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        HttpServletRequest request = RequestHolder.getRequest();

        String forwardedPort = JakartaServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Port");
        String forwardedProto = JakartaServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Proto");

        String scheme = StringUtils.isBlank(forwardedProto) ? request.getScheme() : forwardedProto;
        String port = StringUtils.isBlank(forwardedPort) ? String.valueOf(request.getServerPort()) : forwardedPort;
        String serverName = request.getServerName();

        if (port.equals("443") && "http".equalsIgnoreCase(scheme) && StringUtils.isEmpty(forwardedProto)) {
            scheme = "https";
            port = "";
        }

        if (port.equals("443") && "https".equalsIgnoreCase(scheme)) {
            port = "";
        }

        if (port.equals("80") && "http".equalsIgnoreCase(scheme)) {
            port = "";
        }

        if (StringUtils.isBlank(port)) {
            return scheme + "://" + serverName;
        } else {
            return scheme + "://" + serverName + ":" + port;
        }
    }


    /**
     * 获取当前请求的 Origin 请求头
     *
     * @return  Origin 请求头值
     */
    public static String getOriginAddress() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        HttpServletRequest request = RequestHolder.getRequest();
        return JakartaServletUtil.getHeaderIgnoreCase(request, HttpHeaders.ORIGIN);
    }


}