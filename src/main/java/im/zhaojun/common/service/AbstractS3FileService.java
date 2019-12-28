package im.zhaojun.common.service;

import cn.hutool.core.util.URLUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import im.zhaojun.common.model.S3Model;
import im.zhaojun.common.model.dto.FileItemDTO;
import im.zhaojun.common.model.enums.FileTypeEnum;
import im.zhaojun.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhaojun
 * @date 2019/12/26 22:26
 */
public abstract class AbstractS3FileService extends AbstractFileService {

    @Value("${zfile.cache.timeout}")
    private Long timeout;

    @Resource
    protected StorageConfigService storageConfigService;

    protected String basePath;

    protected AmazonS3 s3Client;

    protected S3Model s3Model;

    /**
     * 获取 S3 指定目录下的对象列表
     * @param s3Client  S3 客户端连接
     * @param s3Model   S3 对象
     * @return  指定目录下的对象列表
     * @throws Exception   获取过程中出现的异常
     */
    public List<FileItemDTO> s3FileList(AmazonS3 s3Client, S3Model s3Model) throws Exception {
        String path = StringUtils.removeFirstSeparator(s3Model.getPath());
        String fullPath = StringUtils.removeFirstSeparator(s3Model.getFullPath());
        String bucketName = s3Model.getBucketName();
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
            fileItemDTO.setName(commonPrefix.substring(fullPath.length(), commonPrefix.length() - 1));
            fileItemDTO.setType(FileTypeEnum.FOLDER);
            fileItemDTO.setPath(path);
            fileItemList.add(fileItemDTO);
        }

        return fileItemList;
    }

    /**
     * 获取对象的访问链接, 如果指定了域名, 则替换为自定义域名.
     * @param s3Client  S3 客户端连接
     * @param s3Model   S3 对象
     * @return  S3 对象访问地址
     */
    public String s3ObjectUrl(AmazonS3 s3Client, S3Model s3Model) {
        String fullPath = StringUtils.removeFirstSeparator(s3Model.getFullPath());
        String bucketName = s3Model.getBucketName();
        String domain = s3Model.getDomain();

        Date expirationDate = new Date(System.currentTimeMillis() + timeout * 1000);
        URL url = s3Client.generatePresignedUrl(bucketName, fullPath, expirationDate);

        String defaultUrl = url.toExternalForm();
        if (StringUtils.isNotNullOrEmpty(domain)) {
            defaultUrl = URLUtil.complateUrl(domain, url.getFile());
        }
        return defaultUrl;
    }
}
