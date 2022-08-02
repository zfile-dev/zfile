package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annoation.StorageParamItem;
import im.zhaojun.zfile.admin.model.enums.StorageParamTypeEnum;
import lombok.Getter;

/**
 * 本地存储初始化参数
 *
 * @author zhaojun
 */
@Getter
public class IpfsParam extends ProxyDownloadParam {

	@StorageParamItem(name = "服务器地址",
			defaultValue = "/ip4/127.0.0.1/tcp/5001",
			description = "IPFS服务器API接口")
	private String apiAddr;

	@StorageParamItem(name = "使用网关", type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "默认使用ipfs网关，关闭也使用ipfs网关（暂不支持ipfs://协议头）")
	private boolean isPrivate;

	@StorageParamItem(name = "ipfs网关", required = false, description = "如不配置网关，则使用公用网关, 反之则使用指定网关下载.")
	private String domain;

}