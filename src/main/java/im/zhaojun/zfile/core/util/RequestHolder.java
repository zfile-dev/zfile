package im.zhaojun.zfile.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Function;

/**
 * 获取 Request 工具类
 *
 * @author zhaojun
 */
public class RequestHolder {

    /**
     * 获取 HttpServletRequest
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest(){
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }


    /**
     * 获取 HttpServletResponse
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse(){
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }


    /**
     * 向 response 写入文件流.
     *
     * @param   function
     *          文件输入流获取函数
     *
     * @param   path
     *          文件路径
     */
    public static void writeFile(Function<String, InputStream> function, String path){
        try (InputStream inputStream = function.apply(path)) {
            HttpServletResponse response = RequestHolder.getResponse();
            String fileName = FileUtil.getName(path);

            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + StringUtils.encodeAllIgnoreSlashes(fileName));
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM.getType());

            OutputStream outputStream = response.getOutputStream();

            IoUtil.copy(inputStream, outputStream);
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}