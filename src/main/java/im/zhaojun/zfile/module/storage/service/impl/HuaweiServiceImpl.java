package im.zhaojun.zfile.module.storage.service.impl;

import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.HuaweiParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class HuaweiServiceImpl extends AbstractS3BaseFileService<HuaweiParam> {

    @Override
    public void init() {
        String endPoint = param.getEndPoint();
        String endPointScheme = param.getEndPointScheme();
        // 如果 endPoint 不包含协议部分, 且配置了 endPointScheme, 则手动拼接协议部分.
        if (!UrlUtils.hasScheme(endPoint) && StringUtils.isNotBlank(endPointScheme)) {
            endPoint = endPointScheme + "://" + endPoint;
        }

        Region oss = Region.of("obs");
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
        return StorageTypeEnum.HUAWEI;
    }

}