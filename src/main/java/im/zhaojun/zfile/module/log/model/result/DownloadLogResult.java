package im.zhaojun.zfile.module.log.model.result;

import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 下载日志结果类
 *
 * @author zhaojun
 */
@Data
public class DownloadLogResult {

	@ApiModelProperty(value="")
	private Integer id;

	@ApiModelProperty(value="文件路径")
	private String path;

	@ApiModelProperty(value = "存储源类型")
	private StorageTypeEnum storageType;
	
	@ApiModelProperty(value = "存储源名称", example = "我的本地存储")
	private String storageName;
	
	@ApiModelProperty(value="访问时间")
	private Date createTime;

	@ApiModelProperty(value="访问 ip")
	private String ip;
	
	@ApiModelProperty(value = "短链 Key")
	private String shortKey;

	@ApiModelProperty(value="访问 user_agent")
	private String userAgent;

	@ApiModelProperty(value="访问 referer")
	private String referer;

}