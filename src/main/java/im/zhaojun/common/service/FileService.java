package im.zhaojun.common.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import im.zhaojun.common.config.ZfileCacheConfiguration;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.AudioInfo;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.ImageInfo;
import im.zhaojun.common.util.AudioHelper;
import im.zhaojun.common.util.SpringContextHolder;
import im.zhaojun.common.util.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@CacheConfig(cacheNames = ZfileCacheConfiguration.CACHE_NAME, keyGenerator = "keyGenerator")
public interface FileService {

    @Cacheable
    List<FileItem> fileList(String path) throws Exception;

    @Cacheable
    String getDownloadUrl(String path) throws Exception;

    /**
     * 获取文件内容.
     */
    default String getTextContent(String url) throws Exception {
        RestTemplate restTemplate = SpringContextHolder.getBean(RestTemplate.class);
        String result = restTemplate.getForObject(url, String.class);
        return result == null ? "" : result;
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
        URL urlObject = new URL(url);
        String originPath = urlObject.getPath();
        url = url.replace(originPath, URLUtil.encode(originPath));
        InputStream inputStream = new URL(url).openStream();
        BufferedImage sourceImg = ImageIO.read(inputStream);
        return new ImageInfo(sourceImg.getWidth(), sourceImg.getHeight());
    }

    default AudioInfo getAudioInfo(String url) throws Exception {
        String query = new URL(URLUtil.decode(url)).getQuery();

        if (query != null) {
            url = url.replace(query, URLUtil.encode(query));
        }

        File file = new File(System.getProperty("user.home") + "/zfile/tmp/audio/" + UUID.fastUUID());
        FileUtil.mkParentDirs(file);
        HttpUtil.downloadFile(url, file);
        AudioInfo audioInfo = AudioHelper.parseAudioInfo(file);
        audioInfo.setSrc(url);
        file.deleteOnExit();
        return audioInfo;
    }


    default List<FileItem> search(String name) throws Exception {
        List<FileItem> result = new ArrayList<>();

        List<FileItem> fileItemList = selectAllFileList();
        for (FileItem fileItem : fileItemList) {
            if (fileItem.getName().contains(name)) {
                result.add(fileItem);
            }
        }

        return result;
    }

    default List<FileItem> selectAllFileList() throws Exception {
        List<FileItem> result = new ArrayList<>();

        String path = "/";

        FileService currentFileService = (FileService) AopContext.currentProxy();
        List<FileItem> fileItemList = currentFileService.fileList(path);
        ArrayDeque<FileItem> queue = new ArrayDeque<>(fileItemList);

        while (!queue.isEmpty()) {
            FileItem fileItem = queue.pop();
            result.add(fileItem);
            if (fileItem.getType() == FileTypeEnum.FOLDER) {
                String filePath = StringUtils.removeDuplicateSeparator("/" + fileItem.getPath() + "/" + fileItem.getName() + "/");
                queue.addAll(currentFileService.fileList(filePath));
            }
        }

        return result;
    }

    StorageTypeEnum getStorageTypeEnum();
}
