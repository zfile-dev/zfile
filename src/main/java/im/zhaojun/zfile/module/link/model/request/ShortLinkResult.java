package im.zhaojun.zfile.module.link.model.request;

import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 短链结果类
 *
 * @author zhaojun
 */
@Data
public class ShortLinkResult {

	@ApiModelProperty(value = "短链 id", example = "1")
	private Integer id;

	@ApiModelProperty(value = "存储源名称", example = "我的本地存储")
	private String storageName;

	@ApiModelProperty(value = "存储源类型")
	private StorageTypeEnum storageType;

	@ApiModelProperty(value = "短链 key", example = "voldd3")
	private String shortKey;

	@ApiModelProperty(value = "文件 url", example = "/directlink/1/test02.png")
	private String url;

	@ApiModelProperty(value = "创建时间", example = "2021-11-22 10:05")
	private Date createDate;

}