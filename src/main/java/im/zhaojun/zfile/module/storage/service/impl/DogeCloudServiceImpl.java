package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.DogeCloudParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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

        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(param.getS3AccessKey(), param.getS3SecretKey(), param.getS3SessionToken());
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        param.getEndPoint(),
                        "automatic"))
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
        JSONObject credentials = resultJsonObject.getJSONObject("data").getJSONObject("Credentials");

        param.setS3AccessKey(credentials.getString("accessKeyId"));
        param.setS3SecretKey(credentials.getString("secretAccessKey"));
        param.setS3SessionToken(credentials.getString("sessionToken"));

        JSONArray bucketsArray = resultJsonObject.getJSONObject("data").getJSONArray("Buckets");
        if (bucketsArray == null || bucketsArray.size() == 0) {
            throw new IllegalArgumentException("存储空间名称不存在");
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
