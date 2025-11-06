package im.zhaojun.zfile.core.exception;

import lombok.Getter;

/**
 * 异常信息枚举类
 *
 * @author zhaojun
 */
@Getter
public enum ErrorCode {

    /**
     * 系统异常
     */
    SYSTEM_ERROR("50000", "系统异常"),
    INVALID_STORAGE_SOURCE("50001", "无效或初始化失败的存储源"),
    DEMO_SITE_DISABLE_OPERATOR("50002", "演示站点不允许此操作"),

    /**
     * 业务异常 4xxxx.
     * 第二位为 0 时，是系统初始化相关错误
     * 第二位为 1 时，是前台(文件管理)错误
     * 第二位为 2 时，是登录错误
     * 第二位为 3 时，是管理员端错误
     */
    BIZ_ERROR("40000", "操作失败"),
    BIZ_NOT_FOUND("40400", "NOT FOUND"),

    // 第二位为 0 时，是系统初始化相关错误
    BIZ_SYSTEM_ALREADY_INIT("40001", "系统已初始化，请勿重复初始化"),
    BIZ_SYSTEM_INIT_ERROR("40002", "系统初始化错误"),

    // 第二位为 1 时，是前台(文件管理)错误
    BIZ_BAD_REQUEST("41000", "请求参数异常"),
    BIZ_UNSUPPORTED_PROXY_DOWNLOAD("41001", "该存储源不支持代理下载"),
    BIZ_INVALID_SIGNATURE("41002", "签名无效或下载地址已过期"),
    BIZ_PREVIEW_FILE_SIZE_EXCEED("41003", "预览文本文件大小超出系统限制"),
    BIZ_FILE_NOT_EXIST("41004", "文件不存在"),
    BIZ_ACCESS_TOO_FREQUENT("41005", "请求太频繁了，请稍后再试"),
    BIZ_UPLOAD_FILE_NOT_EMPTY("41006", "上传文件不能为空"),
    BIZ_UPLOAD_FILE_ERROR("41010", "上传文件失败"),
    BIZ_UPLOAD_FILE_TIMEOUT_ERROR("41026", "上传文件超时"),
    BIZ_EXPIRE_TIME_ILLEGAL("41007", "过期时间不合法"),
    BIZ_DELETE_FILE_NOT_EMPTY("41008", "非空文件夹不允许删除"),
    BIZ_FILE_PATH_ILLEGAL("41009", "文件名/路径存在安全隐患"),
    BIZ_DIRECT_LINK_NOT_ALLOWED("41011", "当前系统不允许使用直链"),
    BIZ_SHORT_LINK_NOT_ALLOWED("41012", "当前系统不允许使用短链"),
    BIZ_SHORT_LINK_EXPIRED("41013", "短链已失效"),
    BIZ_SHORT_LINK_NOT_FOUNT("41014", "短链不存在"),
    BIZ_DIRECT_LINK_EXPIRED("41015", "直链已失效"),
    BIZ_STORAGE_NOT_SUPPORT_OPERATION("41016", "该存储类型不支持此操作"),
    BIZ_STORAGE_NOT_FOUND("41017", "存储源不存在"),
    BIZ_STORAGE_SOURCE_ILLEGAL_OPERATION("41018", "非法或未授权的操作"),
    BIZ_STORAGE_SOURCE_FILE_FORBIDDEN("41019", "文件目录无访问权限"),
    BIZ_STORAGE_SOURCE_FOLDER_PASSWORD_REQUIRED("41020", "此文件夹需要密码"),
    BIZ_STORAGE_SOURCE_FOLDER_PASSWORD_ERROR("41021", "密码错误"),
    BIZ_INVALID_FILE_NAME("41022", "文件名不合法"),
    BIZ_UNSUPPORTED_OPERATION("41023", "不支持的操作"),
    BIZ_FTP_CLIENT_POOL_FULL("41024", "FTP 客户端连接池已满"),
    BIZ_SFTP_CLIENT_POOL_FULL("41025", "SFTP 客户端连接池已满"),
    BIZ_FOLDER_NOT_EXIST("41026", "文件夹不存在"),
    BIZ_UPLOAD_FILE_TYPE_NOT_ALLOWED("41027", "不允许上传的文件"),
    BIZ_RENAME_FILE_TYPE_NOT_ALLOWED("41028", "不允许重命名到该名称"),
    BIZ_UNSUPPORTED_OPERATION_TYPE("41029", "不支持的操作类型"),
    BIZ_CUSTOM_SHARE_LINK_KEY_FORMAT_ILLEGAL("41030", "自定义分享 key 格式不正确，只能包含字母、数字、下划线和短横线，长度为 3-8 位"),
    BIZ_SHARE_LINK_KEY_ALREADY_EXIST("41031", "分享 key 已存在"),
    BIZ_SHARE_LINK_EXPIRY_MUST_BE_FUTURE("41032", "过期时间必须是未来的时间"),
    BIZ_SHARE_LINK_NOT_EXIST("41033", "分享链接不存在"),
    BIZ_SHARE_LINK_EXPIRED("41034", "分享链接已过期"),
    BIZ_SHARE_PASSWORD_ERROR("41036", "分享密码错误"),
    BIZ_SHARE_FILE_LIST_ERROR("41037", "获取分享文件列表失败"),
    BIZ_SHARE_FILE_DOWNLOAD_ERROR("41038", "获取文件下载地址失败"),
    BIZ_SHARE_FILE_INFO_ERROR("41039", "获取文件信息失败"),

    // 第二位为 2 时，是登录错误
    BIZ_UNAUTHORIZED("42000", "未登录或未授权"),
    BIZ_LOGIN_ERROR("42001", "登录失败, 账号或密码错误"),
    BIZ_VERIFY_CODE_ERROR("42002", "验证码错误或已失效"),

    // 第二位为 3 时，是管理员端错误
    BIZ_ADMIN_ERROR("43000", "操作失败"),
    BIZ_USER_NOT_EXIST("43001", "用户不存在"),
    BIZ_USER_EXIST("43002", "用户已存在"),
    BIZ_PASSWORD_NOT_SAME("43003", "两次密码不一致"),
    BIZ_OLD_PASSWORD_ERROR("43004", "旧密码不匹配"),
    BIZ_DELETE_BUILT_IN_USER("43005", "不能删除内置用户"),
    BIZ_UNSUPPORTED_STORAGE_TYPE("43006", "不支持的存储类型"),
    BIZ_STORAGE_KEY_EXIST("43007", "存储源别名已存在"),
    BIZ_AUTO_GET_SHARE_POINT_SITES_ERROR("43008", "自动获取 SharePoint 网站列表失败"),
    BIZ_ORIGINS_NOT_EMPTY("43009", "请先在 \"站点设置\" 中配置站点域名"),
    BIZ_2FA_CODE_ERROR("43010", "双因素认证验证失败"),
    BIZ_STORAGE_INIT_ERROR("43011", "存储源初始化失败"),
    BIZ_RULE_EXIST("43012", "规则已存在"),
    BIZ_SSO_PROVIDER_EXIST("43013", "单点登录配置已存在"),
    BIZ_SSO_PROVIDER_DISABLED("43014", "此单点登录未启用"),


    /**
     * 通用的无权限异常
     */
    NO_FORBIDDEN("30000", "没有权限"),
    NO_CUSTOM_SHARE_LINK_KEY_PERMISSION("30001", "没有自定义分享链接 key 的权限"),



    /**
     * 授权校验异常
     */
    PRO_AUTH_CODE_EMPTY("20000", "请先去后台 \"基本设置\" 填写 \"授权码\""),
    PRO_CHECK_REFERER_EMPTY("20001", "Referer 无效，请检查服务端设置，20001"), // Referer 无效，请检查服务端设置
    PRO_CHECK_TIME_NO_SYNC("20002", "授权校验失败, 服务器时间异常，20002"), // 授权校验失败, 服务器时间异常.
    PRO_AUTH_CODE_INVALID_ERROR("20003", "授权码无效, 请检查后台 \"站点设置\" 中的 \"授权码\" 20003"),
    PRO_CHECK_UNKNOWN_ERROR("20004", "授权验证异常，未知异常，20098"),
    PRO_MSG_ERROR("20005", null);

    private String code;

    private String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 设置错误码
     *
     * @param code 错误码
     * @return 返回当前枚举
     */
    public ErrorCode setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * 设置错误信息
     *
     * @param message 错误信息
     * @return 返回当前枚举
     */
    public ErrorCode setMessage(String message) {
        this.message = message;
        return this;
    }

}