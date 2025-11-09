package im.zhaojun.zfile.module.storage.service.base;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson2.JSON;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.biz.CorsBizException;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.RequestHolder;
import im.zhaojun.zfile.core.util.RequestUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.module.storage.constant.StorageSourceConnectionProperties;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import im.zhaojun.zfile.module.storage.model.dto.ZFileCORSRule;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.S3BaseParam;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.service.impl.S3ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author zhaojun
 */
@Slf4j
public abstract class AbstractS3BaseFileService<P extends S3BaseParam> extends AbstractProxyTransferService<P> {

    protected S3Client s3ClientNew;

    protected S3Presigner s3Presigner;

    protected S3Presigner s3PresignerDownload;

    public static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    Consumer<AwsRequestOverrideConfiguration.Builder> unlimitTimeoutBuilderConsumer = builder -> builder.apiCallTimeout(Duration.ofDays(30)).build();

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
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getDomain())) {
            return getProxyDownloadUrl(pathAndName, false);
        }
        String bucketName = param.getBucketName();
        String domain = param.getDomain();

        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), pathAndName);

        // 如果不是私有空间, 且指定了加速域名, 则直接返回下载地址.
        if (BooleanUtils.isNotTrue(param.isPrivate()) && StringUtils.isNotEmpty(domain)) {
            return StringUtils.concat(domain, StringUtils.encodeAllIgnoreSlashes(fullPath));
        }

        Integer tokenTime = param.getTokenTime();
        if (param.getTokenTime() == null || param.getTokenTime() < 1) {
            tokenTime = 1800;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .applyMutation(processGeneratePresignedUrlRequest())
                .bucket(bucketName)
                .key(fullPath)
                .build();

        S3Presigner presigner = s3PresignerDownload != null ? s3PresignerDownload : s3Presigner;
        PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofSeconds(tokenTime))
                .build());
        URL url = presignedGetObjectRequest.url();
        String defaultUrl = url.toExternalForm();
        if (StringUtils.isNotEmpty(domain)) {
            String path = url.getFile();
            if (this instanceof S3ServiceImpl) {
                path = path.replaceFirst(bucketName + "/", "");
            }
            defaultUrl = StringUtils.concat(domain, path);
        }
        return defaultUrl;
    }

    public Consumer<GetObjectRequest.Builder> processGeneratePresignedUrlRequest() {
        return builder -> {
        };
    }

    /**
     * 获取 S3 指定目录下的对象列表
     * @param path      路径
     * @return  指定目录下的对象列表
     */
    public List<FileItemResult> s3FileList(String path) {
        String bucketName = param.getBucketName();
        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), path, StringUtils.SLASH);

        List<FileItemResult> fileItemList = new ArrayList<>();

        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(fullPath)
                .maxKeys(1000)
                .delimiter(StringUtils.SLASH)
                .build();
        ListObjectsV2Iterable listObjectsV2Iterable = s3ClientNew.listObjectsV2Paginator(listObjectsV2Request);
        for (S3Object s : listObjectsV2Iterable.contents()) {
            FileItemResult fileItemResult = new FileItemResult();
            if (s.key().equals(fullPath)) {
                continue;
            }
            fileItemResult.setName(s.key().substring(fullPath.length()));
            fileItemResult.setSize(s.size());
            fileItemResult.setTime(Date.from(s.lastModified()));
            fileItemResult.setType(FileTypeEnum.FILE);
            fileItemResult.setPath(path);

            String fullPathAndName = StringUtils.concat(getCurrentUserBasePath(), path, fileItemResult.getName());
            fileItemResult.setUrl(getDownloadUrl(fullPathAndName));

            fileItemList.add(fileItemResult);
        }

        for (CommonPrefix commonPrefix : listObjectsV2Iterable.commonPrefixes()) {
            String commonPrefixStr = commonPrefix.prefix();
            FileItemResult fileItemResult = new FileItemResult();
            fileItemResult.setName(commonPrefixStr.substring(fullPath.length(), commonPrefixStr.length() - 1));
            String name = fileItemResult.getName();
            if (StringUtils.isEmpty(name) || StringUtils.equals(name, StringUtils.SLASH)) {
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
        String fileName = FileUtils.getName(pathAndName);
        String parentPath = FileUtils.getParentPath(pathAndName);

        String trimStartPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), pathAndName);
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(param.getBucketName()).key(trimStartPath).build();
        HeadObjectResponse headObjectResponse;
        try {
            headObjectResponse = s3ClientNew.headObject(headObjectRequest);
        } catch (NoSuchKeyException e) {
            return null;
        }

        FileItemResult fileItemResult = new FileItemResult();
        fileItemResult.setName(fileName);
        fileItemResult.setSize(headObjectResponse.contentLength());
        fileItemResult.setTime(Date.from(headObjectResponse.lastModified()));
        fileItemResult.setType(FileTypeEnum.FILE);
        fileItemResult.setPath(parentPath);
        fileItemResult.setUrl(getDownloadUrl(StringUtils.concat(getCurrentUserBasePath(), pathAndName)));
        return fileItemResult;
    }

    @Override
    public boolean newFolder(String path, String name) {
        name = StringUtils.trimSlashes(name);
        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), path, name, StringUtils.SLASH);
        PutObjectResponse putObjectResponse = s3ClientNew.putObject(PutObjectRequest.builder()
                        .bucket(param.getBucketName())
                        .key(fullPath)
                        .build(),
                RequestBody.empty());

        return putObjectResponse != null && putObjectResponse.sdkHttpResponse().isSuccessful();
    }

    @Override
    public boolean deleteFile(String path, String name) {
        String fullPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), path, name);
        DeleteObjectResponse deleteObjectResponse = s3ClientNew.deleteObject(DeleteObjectRequest.builder()
                .bucket(param.getBucketName())
                .key(fullPath)
                .build());
        return deleteObjectResponse != null && deleteObjectResponse.sdkHttpResponse().isSuccessful();
    }

    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name + StringUtils.SLASH);
    }

    @Override
    public boolean renameFile(String path, String name, String newName) {
        this.copyFile(path, name, path, newName);
        this.deleteFile(path, name);
        return true;
    }

    @Override
    public boolean renameFolder(String path, String name, String newName) {
        throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        if (param.isEnableProxyUpload()) {
            return super.getProxyUploadUrl(path, name);
        }
        String bucketName = param.getBucketName();
        String uploadToPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), path, name);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uploadToPath)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))  // The URL expires in 10 minutes.
                .putObjectRequest(objectRequest)
                .build();


        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);
        URL url = presignedPutObjectRequest.url();
        String urlString = url.toExternalForm();

        String contentType = parseContentTypeByName(name, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        urlString = urlString + (urlString.contains("?") ? "&" : "?") + "Content-Type=" + contentType;
        return urlString;
    }



    @Override
    public boolean copyFile(String path, String name, String targetPath, String targetName) {
        String bucketName = param.getBucketName();

        String srcFilePath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), path, name);
        String distFilePath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), targetPath, targetName);

        CopyObjectResponse copyObjectResponse = s3ClientNew.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(srcFilePath)
                .destinationBucket(bucketName)
                .destinationKey(distFilePath)
                .build());
        return copyObjectResponse != null && copyObjectResponse.sdkHttpResponse().isSuccessful();
    }

    @Override
    public boolean copyFolder(String path, String name, String targetPath, String targetName) {
        throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
    }

    @Override
    public boolean moveFile(String path, String name, String targetPath, String targetName) {
        this.copyFile(path, name, targetPath, targetName);
        this.deleteFile(path, name);
        return true;
    }

    @Override
    public boolean moveFolder(String path, String name, String targetPath, String targetName) {
        throw new BizException(ErrorCode.BIZ_STORAGE_NOT_SUPPORT_OPERATION);
    }

    protected void setUploadCors() {
        try {
            List<ZFileCORSRule> zFileCORSRuleList = JSON.parseArray(param.getCorsConfigList(), ZFileCORSRule.class);
            if (zFileCORSRuleList == null || zFileCORSRuleList.isEmpty()) {
                return;
            }
            Set<CORSRule> s3CORSRuleList = ZFileCORSRule.toCORSRule(zFileCORSRuleList);
            CORSConfiguration corsConfiguration = CORSConfiguration.builder().corsRules(s3CORSRuleList).build();

            s3ClientNew.putBucketCors(PutBucketCorsRequest.builder()
                    .bucket(param.getBucketName())
                    .corsConfiguration(corsConfiguration)
                    .build());
        } catch (Exception e) {
            throw new CorsBizException("设置跨域失败, 请检查配置是否正确. 错误信息: " + e.getMessage(), e);
        }
    }


    @Override
    public void uploadFile(String pathAndName, InputStream inputStream, Long size) throws Exception {
        String contentType = parseContentTypeByName(pathAndName, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        String trimStartPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), getCurrentUserBasePath(), pathAndName);

        s3ClientNew.putObject(PutObjectRequest.builder()
                        .overrideConfiguration(unlimitTimeoutBuilderConsumer)
                        .bucket(param.getBucketName())
                        .key(trimStartPath)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(inputStream, size));
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadToStream(String pathAndName) throws Exception {
        String bucketName = param.getBucketName();
        String trimStartPath = StringUtils.concatTrimStartSlashes(param.getBasePath(), pathAndName);

        HttpRange requestRange = RequestUtils.getRequestRange(RequestHolder.getRequest());

        ResponseInputStream<GetObjectResponse> responseResponseInputStream = s3ClientNew.getObject(GetObjectRequest.builder()
                .overrideConfiguration(unlimitTimeoutBuilderConsumer)
                .bucket(bucketName)
                .key(trimStartPath)
                .range(requestRange != null ? "bytes=" + requestRange.getRangeStart(Integer.MAX_VALUE) + "-" + requestRange.getRangeEnd(Integer.MAX_VALUE) : null)
                .build());

        long fileSize = Convert.toLong(responseResponseInputStream.response().contentLength());
        String fileName = FileUtils.getName(pathAndName);
        RequestHolder.writeFile(responseResponseInputStream, fileName, fileSize, true, param.isProxyLinkForceDownload());
        return null;
    }

    public ClientOverrideConfiguration getClientConfiguration() {
        return ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(StorageSourceConnectionProperties.DEFAULT_CONNECTION_TIMEOUT_SECONDS)) // 设置 API 调用超时时间
                .build();
    }

    @Override
    public StorageSourceMetadata getStorageSourceMetadata() {
        StorageSourceMetadata storageSourceMetadata = new StorageSourceMetadata();
        if (param.isEnableProxyUpload()) {
            storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.PROXY);
        } else {
            storageSourceMetadata.setUploadType(StorageSourceMetadata.UploadType.S3);
        }
        storageSourceMetadata.setSupportRenameFolder(false);
        storageSourceMetadata.setSupportMoveFolder(false);
        storageSourceMetadata.setSupportCopyFolder(false);
        storageSourceMetadata.setSupportDeleteNotEmptyFolder(false);
        storageSourceMetadata.setNeedCreateFolderBeforeUpload(false);
        return storageSourceMetadata;
    }

    private static String parseContentTypeByName(String pathAndName, String defaultContentType) {
        String contentType = URLConnection.guessContentTypeFromName(pathAndName);
        if (StringUtils.isBlank(contentType)) {
            contentType = defaultContentType;
        }
        return contentType;
    }

    @Override
    public void destroy() {
        if (this.s3ClientNew != null) {
            this.s3ClientNew.close();
        }
        if (this.s3Presigner != null) {
            this.s3Presigner.close();
        }
        if (this.s3PresignerDownload != null) {
            this.s3PresignerDownload.close();
        }
    }
}
