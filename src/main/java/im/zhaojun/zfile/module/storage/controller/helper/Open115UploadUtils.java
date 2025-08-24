package im.zhaojun.zfile.module.storage.controller.helper;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class Open115UploadUtils {

    private static final String BASE_URL = "https://proapi.115.com";

    private static final String INIT_UPLOAD_PATH = "/open/upload/init";

    private static final String GET_TOKEN_PATH = "/open/upload/get_token";

    private static final Integer FAST_UPLOAD_STATUS = 2;

    private static final Integer NORMAL_UPLOAD_STATUS = 1;

    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * S3 临时上传凭证有效时间为 1 小时，设置为 55 分钟的缓存时间
     */
    private static final Cache<String, UploadTokenResponse> UPLOAD_TOKEN_CACHE = CacheUtil.newTimedCache(55 * 60 * 1000);

    /**
     * 上传文件到 115，自动适应秒传，非秒传场景.
     *
     * @param   file
     *          要上传的文件
     *
     * @param   targetDirId
     *          目标文件夹 ID，0 代表根目录
     *
     * @param   accessTokenSupplier
     *          115 OPEN API 的访问令牌提供者，通常是一个 lambda 表达式或方法引用，这里使用 supplier 是为了防止上传过程中访问令牌过期导致的错误。
     *
     * @return  文件上传成功后返回的 pick_code，如果是秒传则返回对应的 pick_code。
     */
    public static String uploadFile(File file, String fileName, String targetDirId, Supplier<String> accessTokenSupplier) throws Exception {
        InitUploadResponse initResponse = initUploadWithAuthHandling(file, fileName, targetDirId, accessTokenSupplier);
        InitUploadResponse.Data initData = initResponse.getData();

        if (initData.status == FAST_UPLOAD_STATUS) {
            log.info("文件 {} 秒传成功，pick_code: {}", fileName, initData.pickCode);
            return initData.pickCode;
        }

        if (initData.status == NORMAL_UPLOAD_STATUS) {
            log.info("文件 {} 需要正常上传，pick_code: {}", fileName, initData.pickCode);
            UploadTokenResponse tokenResponse = getUploadToken(accessTokenSupplier);
            uploadToObjectStorage(file, initData, tokenResponse.getData());
            log.info("文件 {} 上传到对象存储成功...", fileName);
            return initData.pickCode;
        }

        throw new Exception("上传初始化后出现未处理的上传状态: " + initData.status);
    }

    /**
     * 调用初始化接口，并内置了二次验证的处理逻辑。
     */
    private static InitUploadResponse initUploadWithAuthHandling(File file, String fileName, String targetDirId, Supplier<String> accessToken) throws Exception {
        String fileSha1 = calculateSha1(file, 0, file.length());
        String target = "U_1_" + targetDirId;

        String signKey = null;
        String signVal = null;

        while (true) {
            Map<String, Object> formMap = new HashMap<>();
            formMap.put("file_name", fileName);
            formMap.put("file_size", file.length());
            formMap.put("target", target);
            formMap.put("fileid", fileSha1);

            if (signKey != null && signVal != null) {
                formMap.put("sign_key", signKey);
                formMap.put("sign_val", signVal);
            }

            // https://www.yuque.com/115yun/open/ul4mrauo5i2uza0q
            HttpResponse response = HttpRequest.post(BASE_URL + INIT_UPLOAD_PATH)
                    .bearerAuth(accessToken.get())
                    .form(formMap)
                    .execute();

            String responseBody = response.body();
            InitUploadResponse initResponse = JSON.parseObject(responseBody, InitUploadResponse.class);

            if (!initResponse.state && initResponse.code != 0) {
                throw new Exception("初始化上传接口返回错误: " + initResponse.message);
            }

            InitUploadResponse.Data data = initResponse.getData();
            if (data.status == 7 && data.code == 701) {
                signKey = data.signKey;

                String[] range = data.signCheck.split("-");
                long start = Long.parseLong(range[0]);
                long end = Long.parseLong(range[1]);
                signVal = calculateSha1(file, start, (end - start + 1));
                continue;
            }

            return initResponse;
        }
    }

    /**
     * 直传文件到 open115 提供的对象存储。
     */
    private static void uploadToObjectStorage(File file, InitUploadResponse.Data initData, UploadTokenResponse.Data tokenData) throws Exception {
        CallbackInfo callbackInfo = initData.getCallback();
        try (S3Client s3Client = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(new URI(tokenData.endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsSessionCredentials.create(tokenData.accessKeyId, tokenData.accessKeySecret, tokenData.securityToken)))
                .overrideConfiguration(c -> c.addExecutionInterceptor(new OssHeaderInterceptor()))
                .build()) {

            Map<String, String> metadata = new HashMap<>();
            metadata.put("x-oss-callback", Base64.encode(callbackInfo.callback));
            metadata.put("x-oss-callback-var", Base64.encode(callbackInfo.callbackVar));
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(initData.bucket)
                    .key(initData.object)
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        }
    }

    /**
     * 调用接口获取上传凭证，会使用缓存来避免频繁请求。
     */
    private static UploadTokenResponse getUploadToken(Supplier<String> accessToken) {
        String accessTokenStr = accessToken.get();
        return UPLOAD_TOKEN_CACHE.get(accessTokenStr, false, (Func0<UploadTokenResponse>) () -> {
            // https://www.yuque.com/115yun/open/ul4mrauo5i2uza0q
            HttpResponse response = HttpRequest.get(BASE_URL + GET_TOKEN_PATH)
                    .bearerAuth(accessTokenStr)
                    .execute();

            String responseBody = response.body();
            UploadTokenResponse tokenResponse = JSON.parseObject(responseBody, UploadTokenResponse.class);
            if (!tokenResponse.state) {
                throw new Exception("获取上传凭证接口返回错误: " + tokenResponse.message);
            }
            return tokenResponse;
        });
    }

    /**
     * SHA-1 计算工具方法，支持全文件或文件局部范围计算。
     *
     * @param   file   文件对象
     * @param   offset 开始位置
     * @param   length 要计算的长度
     * @return  大写的 SHA-1 字符串
     */
    private static String calculateSha1(File file, long offset, long length) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            byte[] buffer = new byte[8192];
            int bytesRead;
            long remaining = length;
            while (remaining > 0 && (bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                digest.update(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

    public static class InitUploadResponse {
        public boolean state;
        public String message;
        public int code;
        public Object data;

        public InitUploadResponse.Data getData() {
            if (data instanceof JSONObject jsonObject) {
                return jsonObject.toJavaObject(InitUploadResponse.Data.class);
            }
            return null;
        }

        public static class Data {

            public int status;

            public int code;

            @JSONField(name = "pick_code")
            public String pickCode;

            public String bucket;

            public String object;

            // Object 类型以兼容对象和数组两种情况
            public Object callback;

            @JSONField(name = "sign_key")
            public String signKey;

            @JSONField(name = "sign_check")
            public String signCheck;

            @JSONField(name = "file_id")
            public String fileId;

            public CallbackInfo getCallback() {
                if (callback instanceof JSONObject jsonObject) {
                    return jsonObject.toJavaObject(CallbackInfo.class);
                }
                return null;
            }
        }
    }

    // 这个类只在 status=1 时被使用，此时API返回的是一个对象
    public static class CallbackInfo {

        public String callback;

        @JSONField(name = "callback_var")
        public String callbackVar;

    }

    public static class UploadTokenResponse {

        public boolean state;

        public String message;

        public int code;

        public Object data;

        public UploadTokenResponse.Data getData() {
            if (data instanceof JSONObject jsonObject) {
                return jsonObject.toJavaObject(UploadTokenResponse.Data.class);
            }
            return null;
        }

        public static class Data {

            public String endpoint;

            @JSONField(name = "AccessKeyId")
            public String accessKeyId;

            @JSONField(name = "AccessKeySecret")
            public String accessKeySecret;

            @JSONField(name = "SecurityToken")
            public String securityToken;

            @JSONField(name = "Expiration")
            public String expiration;
        }
    }

    /**
     * 自定义的 S3 执行拦截器，用于处理请求头的转换。去除 amazon s3 sdk 在请求头上自动添加的 `x-amz-meta-` 前缀。
     */
    static class OssHeaderInterceptor implements ExecutionInterceptor {

        @Override
        public SdkHttpRequest modifyHttpRequest(Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
            SdkHttpRequest request = context.httpRequest();

            // 检查是否存在 x-amz-meta-x-oss-callback 头
            Optional<String> callbackHeader = request.firstMatchingHeader("x-amz-meta-x-oss-callback");
            Optional<String> callbackVarHeader = request.firstMatchingHeader("x-amz-meta-x-oss-callback-var");

            // 如果不存在任何一个相关的头，则不进行任何操作
            if (callbackHeader.isEmpty() && callbackVarHeader.isEmpty()) {
                return request;
            }

            SdkHttpRequest.Builder newRequestBuilder = request.toBuilder();

            // 存放需要移除的旧头
            List<String> headersToRemove = new ArrayList<>();

            // 处理 x-oss-callback
            callbackHeader.ifPresent(value -> {
                newRequestBuilder.putHeader("x-oss-callback", value);
                headersToRemove.add("x-amz-meta-x-oss-callback");
            });

            // 处理 x-oss-callback-var
            callbackVarHeader.ifPresent(value -> {
                newRequestBuilder.putHeader("x-oss-callback-var", value);
                headersToRemove.add("x-amz-meta-x-oss-callback-var");
            });

            // 移除旧的 x-amz-meta-* 头
            for (String header : headersToRemove) {
                newRequestBuilder.removeHeader(header);
            }

            return newRequestBuilder.build();
        }
    }

}