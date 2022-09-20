package im.zhaojun.zfile.module.log.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 下载 ip 排行 dto 类
 * @author zhaojun
 */
@Data
public class DownloadTopIpDTO {

	@ApiModelProperty(value = "ip 地址")
	private String ip;

	@ApiModelProperty(value = "下载次数")
	private Integer count;

}