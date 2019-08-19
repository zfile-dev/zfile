package im.zhaojun.common.service;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import im.zhaojun.common.config.CaffeineConfiguration;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.ImageInfo;
import im.zhaojun.common.model.SiteConfig;
import im.zhaojun.common.util.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

@CacheConfig(cacheNames = CaffeineConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public interface FileService {

    @Cacheable
    List<FileItem> fileList(String path) throws Exception;

    @Cacheable
    String getDownloadUrl(String path) throws Exception;

    /**
     * 构建指定路径下标题, 页头, 页尾
     * @param path          路径
     */
    @Cacheable
    default SiteConfig getConfig(String path) throws Exception {
        path = StringUtils.removeLastSeparator(path);
        SiteConfig siteConfig = new SiteConfig();
        for (FileItem fileItem : fileList(path)) {
            if ("readme.md".equalsIgnoreCase(fileItem.getName())) {
                siteConfig.setFooter(getTextContent(path + "/" + fileItem.getName()));
            } else if ("header.md".equalsIgnoreCase(fileItem.getName())) {
                siteConfig.setHeader(getTextContent(path + "/" + fileItem.getName()));
            }
        }
        return siteConfig;
    }

    default String getTextContent(String path) throws Exception {
        return HttpUtil.get(URLDecoder.decode(getDownloadUrl(path), "utf8"));
    }

    @PostConstruct
    default void initMethod() throws Exception {}

    /**
     * 清除缓存.
     */
    @CacheEvict(allEntries = true)
    default void clearCache() throws Exception {
    }

    /**
     * 获取图片信息
     * @param url           图片 URL
     * @return              图片的信息, 宽、高
     */
    @Cacheable
    default ImageInfo getImageInfo(String url) throws Exception {
        url = URLUtil.decode(url);
        InputStream inputStream = new URL(url).openStream();
        BufferedImage sourceImg = ImageIO.read(inputStream);
        return new ImageInfo(sourceImg.getWidth(), sourceImg.getHeight());
    }
}
