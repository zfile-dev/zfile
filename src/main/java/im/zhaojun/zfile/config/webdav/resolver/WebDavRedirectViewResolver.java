
package im.zhaojun.zfile.config.webdav.resolver;

import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.entity.webdav.WebDavFile;
import io.milton.common.View;
import io.milton.http.template.TemplateProcessor;
import io.milton.http.template.ViewResolver;
import io.milton.servlet.OutputStreamWrappingHttpServletResponse;
import io.milton.servlet.ServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * WebDav重定向视图处理器
 * Get注解handler返回字符串时，将使用本ViewResolver处理
 *
 * @author me
 * @date 2022/4/9
 */
public class WebDavRedirectViewResolver implements ViewResolver {

    @Override
    public TemplateProcessor resolveView(View view) {
        return new RedirectTemplateProcessor();
    }

    /**
     * 重定向模板处理程序
     *
     * @author me
     * @date 2022/04/10
     */
    public static class RedirectTemplateProcessor implements TemplateProcessor {

        @Override
        public void execute(Map<String, Object> model, OutputStream out) {
            try {
                // 获取要下载的资源文件
                final Object resource = model.get("resource");
                if (!(resource instanceof WebDavFile)) {
                    throw new RuntimeException("couldn't get direct url.");
                }
                final WebDavFile file = (WebDavFile) resource;
                // 构造文件直链的路径
                final String redirectPath = String.format("/%s/%s%s", ZFileConstant.DIRECT_LINK_PREFIX, file.getDriveId(), file.getFullPath());
                // 重定向到直链
                HttpServletResponse resp = new OutputStreamWrappingHttpServletResponse(ServletResponse.getResponse(), out);
                resp.setStatus(301);
                resp.setHeader("Location", URLUtil.encode(redirectPath));
                resp.setHeader("Connection", "close");
                resp.flushBuffer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
