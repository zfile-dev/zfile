package im.zhaojun.common.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import im.zhaojun.common.exception.NotExistFileException;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.util.StringUtils;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaojun
 * @date 2019/12/26 22:26
 */
public abstract class AbstractS3FileService extends AbstractFileService {

    @Resource
    protected StorageConfigService storageConfigService;

    protected String path;

    protected String basePath;

    protected String bucketName;

    protected String domain;

    protected AmazonS3 s3Client;

    @Override
    public List<FileItemDTO> fileList(String path) {
        this.path = path;
        return s3FileList(path);
    }

    @Override
    public String getDownloadUrl(String path) {
        this.path = path;
        return s3ObjectUrl(path);
    }

    /**
     * 获取 S3 指定目录下的对象列表
     * @param path      路径
     * @return  指定目录下的对象列表
     */
    public List<FileItemDTO> s3FileList(String path) {
        path = StringUtils.removeFirstSeparator(path);
        String fullPath = StringUtils.removeFirstSeparator(getFullPath());
        List<FileItemDTO> fileItemList = new ArrayList<>();
        ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest(bucketName, fullPath, "", "/", 1000));

        for (S3ObjectSummary s : objectListing.getObjectSummaries()) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            if (s.getKey().equals(fullPath)) {
                continue;
            }
            fileItemDTO.setName(s.getKey().substring(fullPath.length()));
            fileItemDTO.setSize(s.getSize());
            fileItemDTO.setTime(s.getLastModified());
            fileItemDTO.setType(FileTypeEnum.FILE);
            fileItemDTO.setPath(path);
            fileItemDTO.setUrl(getDownloadUrl(StringUtils.concatUrl(path, fileItemDTO.getName())));
            fileItemList.add(fileItemDTO);
        }

        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            FileItemDTO fileItemDTO = new FileItemDTO();
            if (Objects.equals(commonPrefix, "/")) {
                continue;
            }
            fileItemDTO.setName(commonPrefix.substring(fullPath.length(), commonPrefix.length() - 1));
            fileItemDTO.setType(FileTypeEnum.FOLDER);
            fileItemDTO.setPath(path);
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    /**
     * 获取对象的访问链接, 如果指定了域名, 则替换为自定义域名.
     * @return  S3 对象访问地址
     */
    public String s3ObjectUrl(String path) {
        String fullPath = StringUtils.removeFirstSeparator(StringUtils.removeDuplicateSeparator(basePath + "/" + path));

        Date expirationDate = new Date(System.currentTimeMillis() + timeout * 1000);
        URL url = s3Client.generatePresignedUrl(bucketName, fullPath, expirationDate);

        String defaultUrl = url.toExternalForm();
        if (StringUtils.isNotNullOrEmpty(domain)) {
            defaultUrl = URLUtil.complateUrl(domain, url.getFile());
        }
        return URLUtil.decode(defaultUrl);
    }

    /**
     * 获取 basePath + path 的全路径地址.
     * @return basePath + path 的全路径地址.
     */
    public String getFullPath() {
        String basePath = ObjectUtil.defaultIfNull(this.basePath, "");
        String path = ObjectUtil.defaultIfNull(this.path, "");
        return StringUtils.removeDuplicateSeparator(basePath + "/" + path);
    }

    @Override
    public FileItemDTO getFileItem(String path) {
        List<FileItemDTO> list = fileList(path);

        if (list == null || list.size() == 0) {
            throw new NotExistFileException();
        }
        return list.get(0);
    }
}