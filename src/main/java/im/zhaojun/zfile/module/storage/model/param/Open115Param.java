package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;

@Getter
public class Open115Param extends OptionalProxyTransferParam {

    @StorageParamItem(name = "AppID", description = "也可自行去 https://open.115.com 申请", defaultValue = "${zfile.open115.appId}", order = 1)
    private String clientId;

    @StorageParamItem(name = "访问令牌", order = 2)
    private String accessToken;

    @StorageParamItem(name = "刷新令牌", order = 3)
    private String refreshToken;

    @StorageParamItem(name = "接口请求速率(秒)", order = 3, defaultValue = "1", type = StorageParamTypeEnum.NUMBER, description = "表示每秒最多允许几个请求，<font color='#e6a23c'>建议最多为 1，过高可能会被风控。且 115 网盘严禁共享使用，多 IP 下载也可能会导致风控/封号，详见：</font><a class='link' target='_blank' href='https://www.yuque.com/115yun/open/vq62qwp8ia2efoli'>https://www.yuque.com/115yun/open/vq62qwp8ia2efoli</a>")
    private Double qps;

    @StorageParamItem(name = "刷新令牌到期时间戳(秒)", hidden = true, required = false)
    private Integer refreshTokenExpiredAt;

    @StorageParamItem(name = "基路径", defaultValue = "/", description = "基路径表示该存储源哪个目录在 ZFile 中作为根目录，如： '/'，'/文件夹1'", order = 6)
    private String basePath;

    @StorageParamItem(hidden = true)
    private boolean enableProxyUpload;

}
