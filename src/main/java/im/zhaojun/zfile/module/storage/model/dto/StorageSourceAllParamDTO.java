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

    @Schema(name = "Endpoint 接入点", example = "oss-cn-beijing.aliyuncs.com")
    private String endPoint;

    @Schema(name = "Endpoint 接入点协议", example = "http")
    private String endPointScheme;

    @Schema(name = "路径风格", example = "path-style")
    private String pathStyle;

    @Schema(name = "是否是私有空间", example = "true")
    private Boolean isPrivate;

    @Schema(name = "代理下载生成签名链接", example = "true")
    private boolean proxyPrivate;

    @Schema(name = "accessKey", example = "LTAI4FjfXqXxQZQZ")
    private String accessKey;

    @Schema(name = "secretKey", example = "QJIO19ASJIKL10ZL")
    private String secretKey;

    @Schema(name = "bucket 名称", example = "zfile-test")
    private String bucketName;

    @Schema(name = "原 bucket 名称", example = "zfile-test")
    private String originBucketName;

    @Schema(name = "域名或 IP", example = "127.0.0.1")
    private String host;

    @Schema(name = "端口", example = "8080")
    private String port;

    @Schema(name = "访问令牌", example = "2.a6b7dbd428f731035f771b8d15063f61.86400.12929220")
    private String accessToken;

    @Schema(name = "刷新令牌", example = "15063f61.86400.1292922000-2346678-1243281asd-1asa")
    private String refreshToken;

    @Schema(name = "secretId", example = "LTAI4FjfXqXxQZQZ")
    private String secretId;

    @Schema(name = "文件路径", example = "/root/")
    private String filePath;

    @Schema(name = "用户名", example = "admin")
    private String username;

    @Schema(name = "密码", example = "123456")
    private String password;

    @Schema(name = "密钥", example = "-----BEGIN OPENSSH PRIVATE KEY-----\nxxxx\n-----END OPENSSH PRIVATE KEY-----")
    private String privateKey;

    @Schema(name = "密钥 passphrase", example = "123456")
    private String passphrase;

    @Schema(name = "域名", example = "http://zfile-test.oss-cn-beijing.aliyuncs.com")
    private String domain;

    @Schema(name = "基路径", example = "/root/")
    private String basePath;

    @Schema(name = "token", example = "12e34awsde12")
    private String token;

    @Schema(name = "token 有效期", example = "1800")
    private Integer tokenTime;

    @Schema(name = "token 有效期", example = "1800")
    private Integer proxyTokenTime;

    @Schema(name = "siteId", example = "ltzx124yu54z")
    private String siteId;

    @Schema(name = "listId", example = "nbmyuoya12sz")
    private String listId;

    @Schema(name = "站点名称", example = "test")
    private String siteName;

    @Schema(name = "站点类型", example = "sites")
    private String siteType;

    @Schema(name = "下载反代域名", example = "http://zfile-oroxy.zfile.vip")
    private String proxyDomain;

    @Schema(name = "下载链接类型", example = "basic")
    private String downloadLinkType;

    @Schema(name = "clientId", example = "4a72d927-1917-418d-9eb2-1b365c53c1c5")
    private String clientId;

    @Schema(name = "clientSecret", example = "l:zI-_yrW75lV8M61K@z.I2K@B/On6Q1a")
    private String clientSecret;

    @Schema(name = "回调地址", example = "https://zfile.jun6.net/onedrive/callback")
    private String redirectUri;

    @Schema(name = "区域", example = "cn-beijing")
    private String region;

    @Schema(name = "url", example = "url 链接")
    private String url;

    @Schema(name = "编码格式", example = "UTF-8")
    private String encoding;

    @Schema(name = "存储源 ID", example = "0AGrY0xF1D7PEUk9PV2")
    private String driveId;

    @Schema(name = "启用代理上传", example = "true")
    private boolean enableProxyUpload;

    @Schema(name = "启用代理下载", example = "true")
    private boolean enableProxyDownload;

    @Schema(name = "下载重定向模式", example = "true")
    private boolean redirectMode;

    @Schema(name = "FTP 模式", example = "passive")
    private String ftpMode;

    @Schema(name = "代理上传超时时间(秒)", example = "300")
    private Integer proxyUploadTimeoutSecond;

    @Schema(name = "最大连接数", example = "8")
    private Integer maxConnections;

    @Schema(name = "下载链接强制下载", example = "true")
    private boolean proxyLinkForceDownload;

    @Schema(name = "S3 跨域配置", example = "[]")
    @JsonSerialize(using = JSONStringSerializer.class)
    @JsonDeserialize(using = JSONStringDeserializer.class)
    private String corsConfigList;

}