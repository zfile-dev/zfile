package im.zhaojun.zfile.service.base;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.URLUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import im.zhaojun.zfile.exception.NotExistFileException;
import im.zhaojun.zfile.model.constant.ZFileConstant;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.enums.FileTypeEnum;
import im.zhaojun.zfile.service.StorageConfigService;
import im.zhaojun.zfile.util.StringUtils;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author zhaojun
 */
public abstract class AbstractS3BaseFileService extends AbstractBaseFileService {

    @Resource
    protected StorageConfigService storageConfigService;

    protected String path;

    protected String bucketName;

    protected String domain;

    protected AmazonS3 s3Client;

    protected boolean isPrivate;

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
        String fullPath = StringUtils.removeFirstSeparator(StringUtils.getFullPath(basePath, path));
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
        basePath = basePath == null ? "" : basePath;
        String fullPath = StringUtils.removeFirstSeparator(StringUtils.removeDuplicateSeparator(basePath + ZFileConstant.PATH_SEPARATOR + path));

        // 如果不是私有空间, 且指定了加速域名, 则直接返回下载地址.
        if (BooleanUtil.isFalse(isPrivate) && StringUtils.isNotNullOrEmpty(domain)) {
            return StringUtils.concatPath(domain, fullPath);
        }

        Date expirationDate = new Date(System.currentTimeMillis() + timeout * 1000);
        URL url = s3Client.generatePresignedUrl(bucketName, fullPath, expirationDate);

        String defaultUrl = url.toExternalForm();
        if (StringUtils.isNotNullOrEmpty(domain)) {
            defaultUrl = URLUtil.complateUrl(domain, url.getFile());
        }
        return URLUtil.decode(defaultUrl);
    }


    @Override
    public FileItemDTO getFileItem(String path) {
        List<FileItemDTO> list;
        try {
            int end = path.lastIndexOf("/");
            list = fileList(path.substring(0, end + 1));
        } catch (Exception e) {
            throw new NotExistFileException();
        }

        for (FileItemDTO fileItemDTO : list) {
            String fullPath = StringUtils.concatUrl(fileItemDTO.getPath(), fileItemDTO.getName());
            if (Objects.equals(fullPath, path)) {
                return fileItemDTO;
            }
        }

        throw new NotExistFileException();
    }

}