package im.zhaojun.zfile.module.log.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 下载文件 排行 dto 类
 *
 * @author zhaojun
 */
@Data
public class DownloadTopFileDTO {

	@ApiModelProperty(value = "短链 key")
	private String shortKey;
	
	@ApiModelProperty(value = "存储源 key")
	private String storageKey;
	
	@ApiModelProperty(value = "文件路径")
	private String path;

	@ApiModelProperty(value = "下载次数")
	private Integer count;

}