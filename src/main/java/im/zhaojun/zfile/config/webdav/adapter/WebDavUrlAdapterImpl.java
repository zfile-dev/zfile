
package im.zhaojun.zfile.config.webdav.adapter;

import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.util.RegexMatchUtils;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.UrlAdapter;

import java.util.regex.Matcher;

/**
 * WebDav路径适配器实现
 *
 * @author me
 * @date 2022/4/10
 */
public class WebDavUrlAdapterImpl implements UrlAdapter {

    /**
     * 获取url
     * eg: domain.com/{webdavPrefix}/{driveId}/{folders}
     *
     * @param request 请求
     * @return {@link String}
     */
    @Override
    public String getUrl(Request request) {
        // 匹配url前缀和驱动器ID
        Matcher matcher = RegexMatchUtils.match("^" + ZFileConstant.WEB_DAV_PREFIX + "/(\\d+)(.*)",
                HttpManager.decodeUrl(request.getAbsolutePath()));
        final String driveId = RegexMatchUtils.getIndexResult(matcher, 1);
        if (driveId == null) {
            return "";
        }
        // 获取摘除前缀和驱动器ID后的文件路径
        final String realPath = RegexMatchUtils.getIndexResult(matcher, 2);
        return realPath != null ? realPath : "";
    }

}
