package im.zhaojun.local.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import im.zhaojun.common.enums.FileTypeEnum;
import im.zhaojun.common.enums.StorageTypeEnum;
import im.zhaojun.common.model.AudioInfo;
import im.zhaojun.common.model.FileItem;
import im.zhaojun.common.model.ImageInfo;
import im.zhaojun.common.model.StorageConfig;
import im.zhaojun.common.service.FileService;
import im.zhaojun.common.service.StorageConfigService;
import im.zhaojun.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LocalService implements FileService {

    private static final String FILE_PATH_KEY = "filePath";

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
            fileItemList.add(fileItem);
            fileItem.setPath(path);
        }

        return fileItemList;
    }

    @Override
    public String getDownloadUrl(String path) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 网络协议
        String networkProtocol = request.getScheme();
        // 网络ip
        String host = request.getServerName();
        // 端口号
        int port = request.getServerPort();
        // 项目发布名称
        String webApp = request.getContextPath();
        return StringUtils.concatPath(networkProtocol + "://" + host + ":" + port + webApp, "local-download?fileName=" + path);
    }

    @Override
    public ImageInfo getImageInfo(String url) throws Exception {
        String query = new URL(URLUtil.decode(url)).getQuery();
        url = url.replace(query, URLUtil.encode(query));
        InputStream inputStream = new URL(url).openStream();
        BufferedImage sourceImg = ImageIO.read(inputStream);
        return new ImageInfo(sourceImg.getWidth(), sourceImg.getHeight());
    }

    @Override
    public String getTextContent(String path) throws Exception {
        return FileUtil.readUtf8String(StringUtils.concatPath(filePath, URLUtil.decode(path)));
    }

    @Override
    public AudioInfo getAudioInfo(String url) throws Exception {
        String query = new URL(URLUtil.decode(url)).getQuery();
        url = url.replace(query, URLUtil.encode(query));
        File file = new File(System.getProperty("user.home") + "/zfile/tmp/audio/" + UUID.fastUUID());
        FileUtil.mkParentDirs(file);
        HttpUtil.downloadFile(url, file);
        Mp3File mp3file = new Mp3File(file);
        ID3v2 audioTag = mp3file.getId3v2Tag();
        String imageMimeType = audioTag.getAlbumImageMimeType();
        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setArtist(audioTag.getArtist());
        audioInfo.setTitle(audioTag.getTitle());
        audioInfo.setCover("data:" + imageMimeType + ";base64," + Base64.encode(audioTag.getAlbumImage()));
        audioInfo.setSrc(url);
        file.deleteOnExit();
        return audioInfo;
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