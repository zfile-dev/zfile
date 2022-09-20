package im.zhaojun.zfile.module.storage.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 所有存储源的全部参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "存储源所有拓展参数")
public class StorageSourceAllParamDTO {

    @ApiModelProperty(value = "Endpoint 接入点", example = "oss-cn-beijing.aliyuncs.com")
    private String endPoint;

    @ApiModelProperty(value = "路径风格", example = "path-style")
    private String pathStyle;

    @ApiModelProperty(value = "是否是私有空间", example = "true")
    private Boolean isPrivate;

    @ApiModelProperty(value = "accessKey", example = "LTAI4FjfXqXxQZQZ")
    private String accessKey;

    @ApiModelProperty(value = "secretKey", example = "QJIO19ASJIKL10ZL")
    private String secretKey;

    @ApiModelProperty(value = "bucket 名称", example = "zfile-test")
    private String bucketName;

    @ApiModelProperty(value = "域名或 IP", example = "127.0.0.1")
    private String host;

    @ApiModelProperty(value = "端口", example = "8080")
    private String port;

    @ApiModelProperty(value = "访问令牌", example = "2.a6b7dbd428f731035f771b8d15063f61.86400.12929220")
    private String accessToken;

    @ApiModelProperty(value = "刷新令牌", example = "15063f61.86400.1292922000-2346678-1243281asd-1asa")
    private String refreshToken;

    @ApiModelProperty(value = "secretId", example = "LTAI4FjfXqXxQZQZ")
    private String secretId;

    @ApiModelProperty(value = "文件路径", example = "/root/")
    private String filePath;

    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;

    @ApiModelProperty(value = "密码", example = "123456")
    private String password;

    @ApiModelProperty(value = "域名", example = "http://zfile-test.oss-cn-beijing.aliyuncs.com")
    private String domain;

    @ApiModelProperty(value = "基路径", example = "/root/")
    private String basePath;

    @ApiModelProperty(value = "token", example = "12e34awsde12")
    private String token;

    @ApiModelProperty(value = "token 有效期", example = "1800")
    private Integer tokenTime;

    @ApiModelProperty(value = "siteId", example = "ltzx124yu54z")
    private String siteId;

    @ApiModelProperty(value = "listId", example = "nbmyuoya12sz")
    private String listId;

    @ApiModelProperty(value = "站点名称", example = "test")
    private String siteName;

    @ApiModelProperty(value = "站点类型", example = "sites")
    private String siteType;

    @ApiModelProperty(value = "下载反代域名", example = "http://zfile-oroxy.zfile.vip")
    private String proxyDomain;

    @ApiModelProperty(value = "下载链接类型", example = "basic")
    private String downloadLinkType;

    @ApiModelProperty(value = "clientId", example = "4a72d927-1917-418d-9eb2-1b365c53c1c5")
    private String clientId;

    @ApiModelProperty(value = "clientSecret", example = "l:zI-_yrW75lV8M61K@z.I2K@B/On6Q1a")
    private String clientSecret;
    
    @ApiModelProperty(value = "回调地址", example = "https://zfile.jun6.net/onedrive/callback")
    private String redirectUri;

    @ApiModelProperty(value = "区域", example = "cn-beijing")
    private String region;

    @ApiModelProperty(value = "url", example = "url 链接")
    private String url;

    @ApiModelProperty(value = "是否自动配置 cors 规则", example = "true")
    private Boolean autoConfigCors;

    @ApiModelProperty(value = "编码格式", example = "UTF-8")
    private String encoding;
    
    @ApiModelProperty(value = "存储源 ID", example = "0AGrY0xF1D7PEUk9PV2")
    private String driveId;
    
}