package im.zhaojun.local.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.ImageInfo;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LocalService implements FileService {

    private static final String FILE_PATH_KEY = "filePath";

    @Value("${server.port}")
    private String port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private StorageConfigService storageConfigService;

    private String filePath;

    @Override
    public void initMethod() {
        Map<String, StorageConfig> stringStorageConfigMap =
                storageConfigService.selectStorageConfigMapByKey(StorageTypeEnum.LOCAL);
        filePath = stringStorageConfigMap.get(FILE_PATH_KEY).getValue();
    }

    @Override
    public List<FileItem> fileList(String path) throws Exception {
        List<FileItem> fileItemList = new ArrayList<>();

        String fullPath = StringUtils.concatPath(filePath, path);

        File file = new File(fullPath);
        File[] files = file.listFiles();

        if (files == null) {
            return fileItemList;
        }
        for (File f : files) {
            FileItem fileItem = new FileItem();
            fileItem.setType(f.isDirectory() ? FileTypeEnum.FOLDER : FileTypeEnum.FILE);
            fileItem.setTime(new Date(f.lastModified()));
            fileItem.setSize(f.length());
            fileItem.setName(f.getName());
            fileItem.setPath(path);
            if (!f.isDirectory()) {
                fileItem.setUrl(getDownloadUrl(StringUtils.concatURL(path, f.getName())));
            }
            fileItemList.add(fileItem);
        }

        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) throws Exception {
        InetAddress localHost = Inet4Address.getLocalHost();
        String host = localHost.getHostAddress();
        return StringUtils.concatPath( "http://" + host + ":" + port + contextPath, "local-download?fileName=" + path);
    }

    @Override
    public ImageInfo getImageInfo(String url) throws Exception {
        String query = new URL(URLUtil.decode(url)).getQuery();
        url = url.replace(query, URLUtil.encode(query));
        InputStream inputStream = new URL(url).openStream();
        BufferedImage sourceImg = ImageIO.read(inputStream);
        return new ImageInfo(sourceImg.getWidth(), sourceImg.getHeight());
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.LOCAL;
    }
}