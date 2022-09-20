package im.zhaojun.zfile.module.config.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.zfile.module.config.model.enums.FileClickModeEnum;
import im.zhaojun.zfile.module.login.model.enums.LoginVerifyModeEnum;
import im.zhaojun.zfile.module.link.model.enums.RefererTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统设置传输类
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "系统设置类")
public class SystemConfigDTO {

    @JsonIgnore
    @ApiModelProperty(value = "ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "站点名称", example = "ZFile Site Name")
    private String siteName;

    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;

    @ApiModelProperty(value = "头像地址", example = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png")
    private String avatar;

    @ApiModelProperty(value = "备案号", example = "冀ICP备12345678号-1")
    private String icp;

    @JsonIgnore
    private String password;

    @ApiModelProperty(value = "站点域名", example = "https://zfile.vip")
    private String domain;

    @ApiModelProperty(value = "自定义 JS")
    private String customJs;

    @ApiModelProperty(value = "自定义 CSS")
    private String customCss;

    @ApiModelProperty(value = "列表尺寸", notes = "large:大,default:中,small:小", example = "default")
    private String tableSize;

    @ApiModelProperty(value = "是否显示文档区", example = "true")
    private Boolean showDocument;

    @ApiModelProperty(value = "网站公告", example = "ZFile 网站公告")
    private String announcement;

    @ApiModelProperty(value = "是否显示网站公告", example = "true")
    private Boolean showAnnouncement;

    @ApiModelProperty(value = "页面布局", notes = "full:全屏,center:居中", example = "full")
    private String layout;

    @ApiModelProperty(value = "是否显示生成直链功能（含直链和路径短链）", example = "true")
    private Boolean showLinkBtn;

    @ApiModelProperty(value = "是否显示生成短链功能", example = "true")
    private Boolean showShortLink;

    @ApiModelProperty(value = "是否显示生成路径链接功能", example = "true")
    private Boolean showPathLink;

    @ApiModelProperty(value = "是否已初始化", example = "true")
    private Boolean installed;

    @ApiModelProperty(value = "自定义视频文件后缀格式")
    private String customVideoSuffix;

    @ApiModelProperty(value = "自定义图像文件后缀格式")
    private String customImageSuffix;

    @ApiModelProperty(value = "自定义音频文件后缀格式")
    private String customAudioSuffix;

    @ApiModelProperty(value = "自定义文本文件后缀格式")
    private String customTextSuffix;

    @ApiModelProperty(value = "直链地址前缀")
    private String directLinkPrefix;

    @ApiModelProperty(value = "直链 Referer 防盗链类型")
    private RefererTypeEnum refererType;

    @ApiModelProperty(value = "是否记录下载日志", example = "true")
    private Boolean recordDownloadLog;

    @ApiModelProperty(value = "直链 Referer 是否允许为空")
    private Boolean refererAllowEmpty;

    @ApiModelProperty(value = "直链 Referer 值")
    private String refererValue;

    @ApiModelProperty(value = "登陆验证方式，支持验证码和 2FA 认证")
    private LoginVerifyModeEnum loginVerifyMode;

    @ApiModelProperty(value = "登陆验证 Secret")
    private String loginVerifySecret;

    @ApiModelProperty(value = "根目录是否显示所有存储源", notes = "根目录是否显示所有存储源, 如果为 true, 则根目录显示所有存储源列表, 如果为 false, 则会自动跳转到第一个存储源.", example = "true", required = true)
    private Boolean rootShowStorage;

    @ApiModelProperty(value = "前端域名", notes = "前端域名，前后端分离情况下需要配置.", example = "http://xxx.example.com")
    private String frontDomain;

    @ApiModelProperty(value = "是否在前台显示登陆按钮", example = "true")
    private Boolean showLogin;

    @ApiModelProperty(value = "RAS Hex Key", example = "r2HKbzc1DfvOs5uHhLn7pA==")
    private String rsaHexKey;

    @ApiModelProperty(value = "默认文件点击习惯", example = "click")
    private FileClickModeEnum fileClickMode;
    
    @ApiModelProperty(value = "最大同时上传文件数", example = "5")
    private Integer maxFileUploads;
    
    @ApiModelProperty(value = "onlyOffice 在线预览地址", example = "http://office.zfile.vip")
    private String onlyOfficeUrl;
    
    @ApiModelProperty(value = "是否允许路径直链可直接访问", example = "true", required = true)
    private Boolean allowPathLinkAnonAccess;
    
}