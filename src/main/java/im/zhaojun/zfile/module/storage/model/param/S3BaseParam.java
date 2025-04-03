package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import im.zhaojun.zfile.module.storage.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.Getter;

/**
 * S3 通用参数
 *
 * @author zhaojun
 */
@Getter
public class S3BaseParam extends OptionalProxyTransferParam {

	@StorageParamItem(name = "AccessKey", order = 10)
	private String accessKey;

	@StorageParamItem(name = "SecretKey", order = 20)
	private String secretKey;

	@StorageParamItem(name = "区域", order = 30, description = "如下拉列表中没有的区域，或想使用内网地址，可直接输入后回车，如: xxx-cn-beijing.example.com")
	private String endPoint;

	@StorageParamItem(name = "EndPoint 协议", order = 31, description = "指定 EndPoint 使用的协议, 默认为 http",
			type = StorageParamTypeEnum.SELECT,
			options = {
				@StorageParamSelectOption(label = "http", value = "http"),
				@StorageParamSelectOption(label = "https", value = "https")
			},
			defaultValue = "http")
	private String endPointScheme;

	@StorageParamItem(name = "存储空间名称", order = 40)
	private String bucketName;

	@StorageParamItem(name = "基路径", order = 50, required = false, defaultValue = "/", description = "基路径表示该存储源哪个目录在 ZFile 中作为根目录，如： '/'，'/文件夹1'")
	private String basePath;

	@StorageParamItem(name = "Bucket 域名 / CDN 加速域名", required = false, order = 60)
	private String domain;

	@StorageParamItem(name = "是否是私有空间", order = 70, type = StorageParamTypeEnum.SWITCH, defaultValue = "true", description = "私有空间会生成带签名的下载链接")
	private boolean isPrivate;

	@StorageParamItem(name = "下载签名有效期", order = 80, condition = "isPrivate==true", required = false, defaultValue = "1800", description = "当为私有空间时, 用于下载签名的有效期, 单位为秒, 如不配置则默认为 1800 秒.")
	private Integer tokenTime;

	@StorageParamItem(name = "跨域配置", order = 200, defaultValue = "[]")
	private String corsConfigList;

}