package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlPath;
import cn.hutool.core.util.URLUtil;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.AliyunParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.net.util.Base64;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class AliyunServiceImpl extends AbstractS3BaseFileService<AliyunParam> {

    private Signer signer;

    @Override
    public void init() {
        String endPoint = param.getEndPoint();
        String endPointScheme = param.getEndPointScheme();
        // 如果 endPoint 不包含协议部分, 且配置了 endPointScheme, 则手动拼接协议部分.
        if (!UrlUtils.hasScheme(endPoint) && StringUtils.isNotBlank(endPointScheme)) {
            endPoint = endPointScheme + "://" + endPoint;
        }
        signer = new Signer(param.getAccessKey(), param.getSecretKey(), endPoint);

        Region oss = Region.of("oss");
        URI endpointOverride = URI.create(endPoint);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(param.getAccessKey(), param.getSecretKey()));

        super.s3ClientNew = S3Client.builder()
                .overrideConfiguration(getClientConfiguration())
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(credentialsProvider)
                .build();

        super.s3Presigner = S3Presigner.builder()
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(credentialsProvider)
                .build();

        setUploadCors();
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.ALIYUN;
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        if (param.isEnableProxyDownload() && StringUtils.isEmpty(param.getDomain())) {
            return getProxyDownloadUrl(pathAndName, false);
        }
        String bucketName = param.getBucketName();
        String domain = param.getDomain();
        String basePath = param.getBasePath();

        String fullPath = StringUtils.concatTrimStartSlashes(basePath, pathAndName);

        // 如果不是私有空间, 且指定了加速域名, 则直接返回下载地址.
        if (BooleanUtils.isNotTrue(param.isPrivate()) && StringUtils.isNotEmpty(domain)) {
            return StringUtils.concat(domain, StringUtils.encodeAllIgnoreSlashes(fullPath));
        }

        Integer tokenTime = param.getTokenTime();
        if (param.getTokenTime() == null || param.getTokenTime() < 1) {
            tokenTime = 1800;
        }

        Date expiration = new Date(System.currentTimeMillis() + tokenTime * 1000);
        String defaultUrl = signer.generatePresignedUrl(bucketName, fullPath, HttpMethod.GET, expiration);

        if (StringUtils.isNotEmpty(domain)) {
            defaultUrl = StringUtils.replaceHost(defaultUrl, domain);
        }
        return defaultUrl;
    }

    static class Signer {

        private final String endPoint;

        private final String accessKey;

        private final String secretKey;

        public Signer(String accessKey, String secretKey, String endPoint) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.endPoint = endPoint;
        }

        private String sign(String data) {
            try {
                SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(signingKey);
                return Base64.encodeBase64StringUnChunked(mac.doFinal(data.getBytes()));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * 生成预签名 URL
         * @param   bucketName
         *          Bucket 名称
         *
         * @param   key
         *          文件路径
         *
         * @param   method
         *          请求方法
         *
         * @param   expiration
         *          过期时间
         *
         * @return  预签名 URL
         */
        private String generatePresignedUrl(String bucketName, String key, HttpMethod method, Date expiration) {
            long expirationLong = expiration.getTime() / 1000;
            UrlBuilder urlBuilder = UrlBuilder.of()
                    .setScheme(UrlUtils.getSchema(endPoint))
                    .setHost(bucketName + "." + UrlUtils.removeScheme(endPoint))
                    .setPath(UrlPath.of(key, StandardCharsets.UTF_8))
                    .addQuery("Expires", expirationLong)
                    .addQuery("OSSAccessKeyId", accessKey);

            String url = StringUtils.concat(bucketName, key);

            String data = method.toString() + "\n\n\n" + expirationLong + "\n" + url;
            String signature = sign(data);
            urlBuilder.addQuery("Signature", URLUtil.encodeAll(signature));

            // 手动编码路径, 防止签名中的特殊字符被 URL 编码
            StringBuilder encodePath = new StringBuilder();
            List<String> split = StringUtils.split(urlBuilder.getPath().toString(), "/", false, true);
            for (String s : split) {
                encodePath.append("/").append(URLUtil.encodeAll(s));
            }

            return urlBuilder.getScheme() + "://" + urlBuilder.getHost() + encodePath + "?" + urlBuilder.getQuery();
        }
    }

}