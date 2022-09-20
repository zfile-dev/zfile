package im.zhaojun.zfile.module.log.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 下载排行请求类
 *
 * @author zhaojun
 */
@Data
public class DownloadTopInfoRequest {

	@ApiModelProperty(value = "排行数量", required = true)
	private Integer top;

	@ApiModelProperty(value = "开始时间")
	private String startTime;

	@ApiModelProperty(value = "结束时间")
	private String endTime;

}