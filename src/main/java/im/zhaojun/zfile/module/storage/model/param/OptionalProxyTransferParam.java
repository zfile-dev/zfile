package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;

@Getter
public class OptionalProxyTransferParam extends ProxyTransferParam {

    @StorageParamItem(name = "代理上传", defaultValue = "false", type = StorageParamTypeEnum.SWITCH, description = "启用该功能后，上传会先上传到服务器，完成后服务器再上传至目标存储源，强依赖服务器带宽大小，请确认必要后开启。", order = 100)
    private boolean enableProxyUpload;

    @StorageParamItem(name = "代理下载", condition = "domain==", defaultValue = "false", type = StorageParamTypeEnum.SWITCH, description = "启用该功能后，下载会先下载到服务器，完成后服务器再下载返回给下载用户，强依赖服务器带宽大小，请确认必要后开启。", order = 101)
    private boolean enableProxyDownload;

    @StorageParamItem(name = "代理下载生成签名链接", condition = "enableProxyDownload==true", onlyOverwrite = { StorageParamItemAnnoEnum.NAME, StorageParamItemAnnoEnum.CONDITION, StorageParamItemAnnoEnum.ORDER }, order = 102)
    private boolean proxyPrivate;

    @StorageParamItem(name = "代理下载签名有效期", condition = "proxyPrivate==true", required = false, defaultValue = "1800", description = "用于下载签名的有效期, 单位为秒, 如不配置则默认为 1800 秒.", order = 103)
    private Integer proxyTokenTime;

    @StorageParamItem(name = "代理下载链接强制下载", condition = "enableProxyDownload==true", type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "控制代理下载时的下载行为：关闭则使用浏览器默认行为，启用则强制下载", order = 105)
    private boolean proxyLinkForceDownload;

}