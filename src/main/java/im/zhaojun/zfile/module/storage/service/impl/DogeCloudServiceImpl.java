package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.DogeCloudParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
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
public class DogeCloudServiceImpl extends AbstractS3BaseFileService<DogeCloudParam> implements RefreshTokenService {

    @Override
    public void init() {
        refreshAccessToken();

        Region oss = Region.of("automatic");
        URI endpointOverride = URI.create(param.getEndPoint());

        super.s3ClientNew = S3Client.builder()
                .overrideConfiguration(getClientConfiguration())
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(() -> AwsSessionCredentials.create(param.getS3AccessKey(), param.getS3SecretKey(), param.getS3SessionToken()))
                .build();

        super.s3Presigner = S3Presigner.builder()
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(() -> AwsSessionCredentials.create(param.getS3AccessKey(), param.getS3SecretKey(), param.getS3SessionToken()))
                .build();


    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.DOGE_CLOUD;
    }

    @Override
    public void refreshAccessToken() {
        JSONObject jsonObject = new JSONObject();
		jsonObject.put("channel", "OSS_FULL");
		jsonObject.put("scopes", param.getOriginBucketName());

        String apiDomain = "https://api.dogecloud.com";
        String apiPath = "/auth/tmp_token.json";

        String token = getToken(apiPath, jsonObject.toJSONString());

        HttpResponse httpResponse = HttpUtil.createPost(apiDomain + apiPath)
                .body(jsonObject.toJSONString())
                .header(Header.AUTHORIZATION, "TOKEN " + param.getAccessKey() + ":" + token).execute();

        String body = httpResponse.body();
        JSONObject resultJsonObject = JSONObject.parseObject(body);
        if (resultJsonObject.getInteger("code") != 200){
            //TODO 根据返回ERR_CODE定义错误类型 https://docs.dogecloud.com/oss/api-introduction?id=%e9%94%99%e8%af%af%e4%bb%a3%e7%a0%81%e5%88%97%e8%a1%a8
            log.error("获取 Token 失败, response: {}", body);
            throw new BizException(resultJsonObject.getString("msg"));
        }
        JSONObject credentials = resultJsonObject.getJSONObject("data").getJSONObject("Credentials");

        param.setS3AccessKey(credentials.getString("accessKeyId"));
        param.setS3SecretKey(credentials.getString("secretAccessKey"));
        param.setS3SessionToken(credentials.getString("sessionToken"));

        JSONArray bucketsArray = resultJsonObject.getJSONObject("data").getJSONArray("Buckets");
        if (bucketsArray == null || bucketsArray.isEmpty()) {
            throw new SystemException("存储空间名称不存在");
        }

        JSONObject buckets = bucketsArray.getJSONObject(0);

        param.setBucketName(buckets.getString("s3Bucket"));
        param.setEndPoint(buckets.getString("s3Endpoint"));
    }


    private String getToken(String apiPath, String paramsText) {
        String signStr = apiPath + "\n" + paramsText;
        return SecureUtil.hmacSha1(param.getSecretKey()).digestHex(signStr);
    }


}