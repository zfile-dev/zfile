package im.zhaojun.zfile.module.storage.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.exception.core.BizException;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.module.storage.model.bo.RefreshTokenCacheBO;
import im.zhaojun.zfile.module.storage.model.dto.RefreshTokenInfoDTO;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.module.storage.model.param.DogeCloudParam;
import im.zhaojun.zfile.module.storage.service.base.AbstractS3BaseFileService;
import im.zhaojun.zfile.module.storage.service.base.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
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

    private AwsCredentials awsCredentials;

    @Override
    public void init() {
        refreshAccessToken();
        Region oss = Region.of("automatic");
        URI endpointOverride = URI.create(param.getEndPoint());

        super.s3ClientNew = S3Client.builder()
                .overrideConfiguration(getClientConfiguration())
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(this::checkExpiredAndGetAwsCredentials)
                .build();

        super.s3Presigner = S3Presigner.builder()
                .region(oss)
                .endpointOverride(endpointOverride)
                .credentialsProvider(this::checkExpiredAndGetAwsCredentials)
                .build();
    }

    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.DOGE_CLOUD;
    }

    @Override
    public void refreshAccessToken() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("channel", "OSS_FULL");
            jsonObject.put("scopes", param.getOriginBucketName());

            String apiDomain = "https://api.dogecloud.com";
            String apiPath = "/auth/tmp_token.json";

            String jsonString = jsonObject.toJSONString();
            String token = getToken(apiPath, jsonString);

            if (log.isDebugEnabled()) {
                log.debug("{} 尝试获取 S3 临时密钥, 请求参数: {}", getStorageSimpleInfo(), param);
            }

            HttpResponse httpResponse = HttpUtil.createPost(apiDomain + apiPath)
                    .body(jsonString)
                    .header(Header.AUTHORIZATION, "TOKEN " + param.getAccessKey() + ":" + token)
                    .execute();

            String body = httpResponse.body();
            int responseStatus = httpResponse.getStatus();

            JSONObject resultJsonObject = JSONObject.parseObject(body);
            if (resultJsonObject.getInteger("code") != 200){
                log.error("{} 获取 S3 临时密钥失败, 响应头: {}", getStorageSimpleInfo(), body);
                throw new BizException(resultJsonObject.getString("msg"));
            }

            if (log.isDebugEnabled()) {
                log.debug("{} 获取 S3 临时密钥完成. 响应状态码: {}, 响应体: {}", getStorageSimpleInfo(), responseStatus, body);
            }

            JSONObject data = resultJsonObject.getJSONObject("data");
            JSONObject credentials = data.getJSONObject("Credentials");
            String s3AccessKey = credentials.getString("accessKeyId");
            String s3SecretKey = credentials.getString("secretAccessKey");
            String s3SessionToken = credentials.getString("sessionToken");
            Integer expiredAt = data.getInteger("ExpiredAt");

            JSONArray bucketsArray = data.getJSONArray("Buckets");
            if (bucketsArray == null || bucketsArray.isEmpty()) {
                throw new SystemException("存储空间名称不存在");
            }
            JSONObject buckets = bucketsArray.getJSONObject(0);
            param.setBucketName(buckets.getString("s3Bucket"));
            param.setEndPoint(buckets.getString("s3Endpoint"));

            RefreshTokenInfoDTO tokenInfoDTO = RefreshTokenInfoDTO.success(s3AccessKey, s3SecretKey, s3SessionToken, expiredAt);
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.success(tokenInfoDTO));

            awsCredentials = AwsSessionCredentials.create(s3AccessKey, s3SecretKey, s3SessionToken);
        } catch (Exception e) {
            RefreshTokenCacheBO.putRefreshTokenInfo(storageId, RefreshTokenCacheBO.RefreshTokenInfo.fail("AccessToken 刷新失败: " + e.getMessage()));
            throw new SystemException("存储源 " + storageId + " 刷新令牌失败, 获取时发生异常.", e);
        }
    }

    private String getToken(String apiPath, String paramsText) {
        String signStr = apiPath + "\n" + paramsText;
        return SecureUtil.hmacSha1(param.getSecretKey()).digestHex(signStr);
    }

    /**
     * 检查 AccessToken 是否过期，并获取最新的 AwsCredentials。
     */
    private AwsCredentials checkExpiredAndGetAwsCredentials() {
        RefreshTokenCacheBO.RefreshTokenInfo refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);

        if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
            // 使用双重检查锁定机制，确保同一个 storageId 只会有一个线程在刷新 AccessToken
            synchronized (("storage-refresh-" + storageId).intern()) {
                // 双重检查，再次从缓存中获取，确认是否其他线程已经刷新过
                refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
                if (refreshTokenInfo == null || refreshTokenInfo.isExpired()) {
                    log.info("{} S3 临时密钥未获取或已过期, 尝试刷新.", getStorageSimpleInfo());
                    refreshAccessToken();
                    refreshTokenInfo = RefreshTokenCacheBO.getRefreshTokenInfo(storageId);
                }
            }
        }

        if (refreshTokenInfo == null) {
            throw new SystemException("存储源 " + storageId + " AccessToken 刷新失败: 未找到刷新令牌信息.");
        }

        return awsCredentials;
    }

}