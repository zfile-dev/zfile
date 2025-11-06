package im.zhaojun.zfile.module.storage.service.impl;

import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.S3Param;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class S3ServiceImpl extends AbstractS3BaseFileService<S3Param> {

    @Override
    public void init() {
        String endPoint = param.getEndPoint();
        String endPointScheme = param.getEndPointScheme();
        // 如果 endPoint 不包含协议部分, 且配置了 endPointScheme, 则手动拼接协议部分.
        if (!UrlUtils.hasScheme(endPoint) && StringUtils.isNotBlank(endPointScheme)) {
            endPoint = endPointScheme + "://" + endPoint;
        }

        boolean isPathStyle = "path-style".equals(param.getPathStyle());
        String domain = param.getDomain();
        if (StringUtils.isNotBlank(domain) && !isPathStyle) {
            throw new BizException("当使用域名访问时, 域名风格只能使用路径模式, 请修改存储配置中的域名风格选项.");
        }

        String region = param.getRegion();
        if (StringUtils.isEmpty(param.getRegion()) && StringUtils.isNotEmpty(endPoint)) {
            region = endPoint.split("\\.")[1];
        }

        Region oss = Region.of(region);
        URI endpointOverride = URI.create(endPoint);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(param.getAccessKey(), param.getSecretKey()));

        super.s3ClientNew = S3Client.builder()
                .overrideConfiguration(getClientConfiguration())
                .forcePathStyle(isPathStyle)
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(credentialsProvider)
                .build();

        super.s3PresignerDownload = S3Presigner.builder()
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(isPathStyle)
                        .build())
                .region(oss)
                .endpointOverride(StringUtils.isBlank(domain) ? endpointOverride : URI.create(domain))
                .credentialsProvider(credentialsProvider)
                .build();

        super.s3Presigner = S3Presigner.builder()
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(isPathStyle)
                        .build())
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(credentialsProvider)
                .build();

        setUploadCors();
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.S3;
    }

}