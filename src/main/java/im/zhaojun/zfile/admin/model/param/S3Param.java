package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annotation.StorageParamItem;
import im.zhaojun.zfile.admin.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.admin.model.enums.StorageParamTypeEnum;
import lombok.Getter;

/**
 * S3 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class S3Param extends S3BaseParam {

	@StorageParamItem(name = "EndPoint", order = 3)
	private String endPoint;

	@StorageParamItem(name = "地域", order = 3)
	private String region;

	@StorageParamItem(name = "域名风格", type = StorageParamTypeEnum.SWITCH,
			options = { @StorageParamSelectOption(value = "path-style", label = "path-style"),
						@StorageParamSelectOption(value = "bucket-virtual-hosting", label = "bucket-virtual-hosting")},
			linkName = "查看 S3 API 说明文档", link = "https://docs.aws.amazon.com/zh_cn/AmazonS3/latest/userguide/VirtualHosting.html#path-style-access")
	private String pathStyle;

}