package im.zhaojun.zfile.module.storage.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import im.zhaojun.zfile.core.config.jackson.JSONStringDeserializer;
import im.zhaojun.zfile.core.config.jackson.JSONStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 所有存储源的全部参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "存储源所有拓展参数")
public class StorageSourceAllParamDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "Endpoint 接入点", example = "oss-cn-beijing.aliyuncs.com")
    private String endPoint;

    @Schema(title = "Endpoint 接入点协议", example = "http")
    private String endPointScheme;

    @Schema(title = "路径风格", example = "path-style")
    private String pathStyle;

    @Schema(title = "是否是私有空间", example = "true")
    private Boolean isPrivate;

    @Schema(title = "代理下载生成签名链接", example = "true")
    private boolean proxyPrivate;

    @Schema(title = "accessKey", example = "LTAI4FjfXqXxQZQZ")
    private String accessKey;

    @Schema(title = "secretKey", example = "QJIO19ASJIKL10ZL")
    private String secretKey;

    @Schema(title = "bucket 名称", example = "zfile-test")
    private String bucketName;

    @Schema(title = "原 bucket 名称", example = "zfile-test")
    private String originBucketName;

    @Schema(title = "域名或 IP", example = "127.0.0.1")
    private String host;

    @Schema(title = "端口", example = "8080")
    private String port;

    @Schema(title = "访问令牌", example = "2.a6b7dbd428f731035f771b8d15063f61.86400.12929220")
    private String accessToken;

    @Schema(title = "刷新令牌", example = "15063f61.86400.1292922000-2346678-1243281asd-1asa")
    private String refreshToken;

    @Schema(title = "刷新令牌到期时间(秒)", example = "1752994685")
    private Integer refreshTokenExpiredAt;

    @Schema(title = "接口请求频率限制", example = "1.5")
    private Double qps;

    @Schema(title = "secretId", example = "LTAI4FjfXqXxQZQZ")
    private String secretId;

    @Schema(title = "文件路径", example = "/root/")
    private String filePath;

    @Schema(title = "用户名", example = "admin")
    private String username;

    @Schema(title = "密码", example = "123456")
    private String password;

    @Schema(title = "密钥", example = "-----BEGIN OPENSSH PRIVATE KEY-----\nxxxx\n-----END OPENSSH PRIVATE KEY-----")
    private String privateKey;

    @Schema(title = "密钥 passphrase", example = "123456")
    private String passphrase;

    @Schema(title = "域名", example = "http://zfile-test.oss-cn-beijing.aliyuncs.com")
    private String domain;

    @Schema(title = "基路径", example = "/root/")
    private String basePath;

    @Schema(title = "token", example = "12e34awsde12")
    private String token;

    @Schema(title = "token 有效期", example = "1800")
    private Integer tokenTime;

    @Schema(title = "token 有效期", example = "1800")
    private Integer proxyTokenTime;

    @Schema(title = "siteId", example = "ltzx124yu54z")
    private String siteId;

    @Schema(title = "listId", example = "nbmyuoya12sz")
    private String listId;

    @Schema(title = "站点名称", example = "test")
    private String siteName;

    @Schema(title = "站点类型", example = "sites")
    private String siteType;

    @Schema(title = "下载反代域名", example = "http://zfile-oroxy.zfile.vip")
    private String proxyDomain;

    @Schema(title = "下载链接类型", example = "basic")
    private String downloadLinkType;

    @Schema(title = "clientId", example = "4a72d927-1917-418d-9eb2-1b365c53c1c5")
    private String clientId;

    @Schema(title = "clientSecret", example = "l:zI-_yrW75lV8M61K@z.I2K@B/On6Q1a")
    private String clientSecret;

    @Schema(title = "回调地址", example = "https://zfile.jun6.net/onedrive/callback")
    private String redirectUri;

    @Schema(title = "区域", example = "cn-beijing")
    private String region;

    @Schema(title = "url", example = "url 链接")
    private String url;

    @Schema(title = "编码格式", example = "UTF-8")
    private String encoding;

    @Schema(title = "存储源 ID", example = "0AGrY0xF1D7PEUk9PV2")
    private String driveId;

    @Schema(title = "启用代理上传", example = "true")
    private boolean enableProxyUpload;

    @Schema(title = "启用代理下载", example = "true")
    private boolean enableProxyDownload;

    @Schema(title = "下载重定向模式", example = "true")
    private boolean redirectMode;

    @Schema(title = "FTP 模式", example = "passive")
    private String ftpMode;

    @Schema(title = "代理上传超时时间(秒)", example = "300")
    private Integer proxyUploadTimeoutSecond;

    @Schema(title = "最大连接数", example = "8")
    private Integer maxConnections;

    @Schema(title = "下载链接强制下载", example = "true")
    private boolean proxyLinkForceDownload;

    @Schema(title = "S3 跨域配置", example = "[]")
    @JsonSerialize(using = JSONStringSerializer.class)
    @JsonDeserialize(using = JSONStringDeserializer.class)
    private String corsConfigList;

}