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
import org.springframework.http.ContentDisposition;
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
import java.nio.charset.StandardCharsets;
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

            ContentDisposition contentDisposition = ContentDisposition
                    .builder(forceDownload ? "attachment" : "inline")
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            if (forceDownload) {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            } else {
                response.setContentType(MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM).toString());
            }

            outputStream = response.getOutputStream();

            if (fileSize != null && fileSize > 0) {
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

        String forwardedHost = JakartaServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Host");
        String forwardedPort = JakartaServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Port");
        String forwardedProto = JakartaServletUtil.getHeaderIgnoreCase(request, "X-Forwarded-Proto");

        String scheme = StringUtils.isBlank(forwardedProto) ? request.getScheme() : forwardedProto;
        
        // 优先使用 X-Forwarded-Host，其次使用 Host 头，最后使用 request.getServerName()
        String serverName;
        String hostHeader = StringUtils.isNotBlank(forwardedHost) ? forwardedHost : request.getHeader("Host");
        if (StringUtils.isNotBlank(hostHeader)) {
            // Host 头可能包含端口信息，如 "example.com:8080"
            String[] hostParts = hostHeader.split(":");
            serverName = hostParts[0];
            // 如果 Host 头包含端口且没有显式设置 X-Forwarded-Port，则使用 Host 头中的端口
            if (hostParts.length > 1 && StringUtils.isBlank(forwardedPort)) {
                forwardedPort = hostParts[1];
            }
        } else {
            serverName = request.getServerName();
        }
        
        // 端口处理逻辑
        String port;
        if (StringUtils.isNotBlank(forwardedPort)) {
            port = forwardedPort;
        } else if (StringUtils.isNotBlank(forwardedProto)) {
            // 如果设置了转发协议但没有设置端口，使用协议默认端口
            port = "https".equalsIgnoreCase(forwardedProto) ? "443" : "80";
        } else {
            port = String.valueOf(request.getServerPort());
        }

        // 移除默认端口
        if ("443".equals(port) && "https".equalsIgnoreCase(scheme)) {
            port = "";
        }
        if ("80".equals(port) && "http".equalsIgnoreCase(scheme)) {
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