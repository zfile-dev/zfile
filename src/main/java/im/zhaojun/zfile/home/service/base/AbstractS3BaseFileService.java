package im.zhaojun.zfile.home.service.base;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest;
import im.zhaojun.zfile.admin.exception.StorageSourceAutoConfigCorsException;
import im.zhaojun.zfile.admin.model.param.S3BaseParam;
import im.zhaojun.zfile.common.constant.ZFileConstant;
import im.zhaojun.zfile.common.exception.file.operator.GetFileInfoException;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractS3BaseFileService<P extends S3BaseParam> extends AbstractBaseFileService<P> {

    protected AmazonS3 s3Client;

    @Override
    public List<FileItemResult> fileList(String folderPath) {
        return s3FileList(folderPath);
    }


    /**
     * 默认 S3 获取对象下载链接的方法, 如果指定了域名, 则替换为自定义域名.
     * @return  S3 对象访问地址
     */
    @Override
    public String getDownloadUrl(String pathAndName) {
        String bucketName = param.getBucketName();
        String domain = param.getDomain();

        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath() + pathAndName);

        // 如果不是私有空间, 且指定了加速域名, 则直接返回下载地址.
        if (BooleanUtil.isFalse(param.isPrivate()) && StrUtil.isNotEmpty(domain)) {
            return StringUtils.concat(domain, fullPath);
        }

        Integer tokenTime = param.getTokenTime();
        if (param.getTokenTime() == null || param.getTokenTime() < 1) {
            tokenTime = 1800;
        }

        Date expirationDate = new Date(System.currentTimeMillis() + tokenTime * 1000);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fullPath, HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expirationDate);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        String defaultUrl = url.toExternalForm();
        if (StrUtil.isNotEmpty(domain)) {
            defaultUrl = StringUtils.concat(domain, url.getFile());
        }
        return defaultUrl;
    }


    /**
     * 获取 S3 指定目录下的对象列表
     * @param path      路径
     * @return  指定目录下的对象列表
     */
    public List<FileItemResult> s3FileList(String path) {
        String bucketName = param.getBucketName();
        path = StringUtils.trimStartSlashes(path);
        String fullPath = StringUtils.trimStartSlashes(StringUtils.concat(param.getBasePath(), path, ZFileConstant.PATH_SEPARATOR));
        List<FileItemResult> fileItemList = new ArrayList<>();
        ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest(bucketName, fullPath, "", "/", 1000));

        for (S3ObjectSummary s : objectListing.getObjectSummaries()) {
            FileItemResult fileItemResult = new FileItemResult();
            if (s.getKey().equals(fullPath)) {
                continue;
            }
            fileItemResult.setName(s.getKey().substring(fullPath.length()));
            fileItemResult.setSize(s.getSize());
            fileItemResult.setTime(s.getLastModified());
            fileItemResult.setType(FileTypeEnum.FILE);
            fileItemResult.setPath(path);

            String fullPathAndName = StringUtils.concat(path, fileItemResult.getName());
            fileItemResult.setUrl(getDownloadUrl(fullPathAndName));

            fileItemList.add(fileItemResult);
        }

        for (String commonPrefix : objectListing.getCommonPrefixes()) {
            FileItemResult fileItemResult = new FileItemResult();
            fileItemResult.setName(commonPrefix.substring(fullPath.length(), commonPrefix.length() - 1));
            String name = fileItemResult.getName();
            if (StrUtil.isEmpty(name) || StrUtil.equals(name, StringUtils.DELIMITER_STR)) {
                continue;
            }

            fileItemResult.setType(FileTypeEnum.FOLDER);
            fileItemResult.setPath(path);
            fileItemList.add(fileItemResult);
        }

        return fileItemList;
    }

    @Override
    public FileItemResult getFileItem(String pathAndName) {
        try {
            String fileName = FileUtil.getName(pathAndName);
            String parentPath = StringUtils.getParentPath(pathAndName);

            String trimStartPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), pathAndName);
            ObjectMetadata objectMetadata = s3Client.getObjectMetadata(param.getBucketName(), trimStartPath);

            FileItemResult fileItemResult = new FileItemResult();
            fileItemResult.setName(fileName);
            fileItemResult.setSize(objectMetadata.getInstanceLength());
            fileItemResult.setTime(objectMetadata.getLastModified());
            fileItemResult.setType(FileTypeEnum.FILE);
            fileItemResult.setPath(parentPath);
            fileItemResult.setUrl(getDownloadUrl(pathAndName));
            return fileItemResult;
        } catch (Exception e) {
            throw new GetFileInfoException(storageId, pathAndName, e);
        }
    }

    @Override
    public boolean newFolder(String path, String name) {
        String bucketName = param.getBucketName();
        name = StringUtils.trimSlashes(name);
        String fullPath = StringUtils.concat(param.getBasePath(), path, name, ZFileConstant.PATH_SEPARATOR);
        fullPath = StringUtils.trimStartSlashes(fullPath);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fullPath, emptyContent, null);
        PutObjectResult putObjectResult = s3Client.putObject(putObjectRequest);
        return putObjectResult != null;
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String bucketName = param.getBucketName();
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);
        fullPath = StringUtils.trimStartSlashes(fullPath);
        try {
            s3Client.deleteObject(bucketName, fullPath);
            return true;
        } catch (Exception e) {
            log.error("存储源 {} 删除文件 {} 失败", storageId, fullPath, e);
        }
        return false;
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        String bucketName = param.getBucketName();
        String fullPath = StringUtils.concat(param.getBasePath(), path, name);
        fullPath = StringUtils.trimStartSlashes(fullPath);
        try {
            s3Client.deleteObject(bucketName, fullPath + '/');
            return true;
        } catch (Exception e) {
            log.error("存储源 {} 删除文件夹 {} 失败", storageId, fullPath, e);
        }
        return false;
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        String bucketName = param.getBucketName();
        String srcPath = StringUtils.concat(param.getBasePath(), path, name);
        srcPath = StringUtils.trimStartSlashes(srcPath);

        String distPath = StringUtils.concat(param.getBasePath(), path, newName);
        distPath = StringUtils.trimStartSlashes(distPath);

        try {
            s3Client.copyObject(bucketName, srcPath, bucketName, distPath);
            deleteFile(path, name);
            return true;
        } catch (Exception e) {
            log.error("存储源 {} 重命名文件 {} 至 {} 失败", storageId, srcPath, distPath, e);
        }

        return false;
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        throw new UnsupportedOperationException("不支持重命名文件夹");
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        String bucketName = param.getBucketName();
        String uploadToPath = StringUtils.concat(param.getBasePath(), path, name);
        uploadToPath = StringUtils.trimStartSlashes(uploadToPath);

        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(bucketName, uploadToPath, HttpMethod.PUT);
        URL url = s3Client.generatePresignedUrl(req);

        return url.toExternalForm();
    }

    protected void setUploadCors() {
        if (param.isAutoConfigCors()) {
            try {
                BucketCrossOriginConfiguration bucketCrossOriginConfiguration = new BucketCrossOriginConfiguration();
                ArrayList<CORSRule> rules = new ArrayList<>();

                CORSRule corsRule = new CORSRule();
                corsRule.setAllowedMethods(CORSRule.AllowedMethods.PUT);
                corsRule.setAllowedOrigins("*");

                rules.add(corsRule);

                bucketCrossOriginConfiguration.setRules(rules);
                SetBucketCrossOriginConfigurationRequest setBucketCrossOriginConfigurationRequest =
                        new SetBucketCrossOriginConfigurationRequest(param.getBucketName(), bucketCrossOriginConfiguration);
                s3Client.setBucketCrossOriginConfiguration(setBucketCrossOriginConfigurationRequest);
            } catch (Exception e) {
                throw new StorageSourceAutoConfigCorsException("设置跨域失败，请检查 API 密钥是否有权限设置跨域", e, param);
            }
        }
    }

}