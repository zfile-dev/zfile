package im.zhaojun.zfile.admin.model.request.link;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 查询下载日志请求参数
 *
 * @author zhaojun
 */
@Data
public class QueryDownloadLogRequest {

	@ApiModelProperty(value="文件路径")
	private String path;

	@ApiModelProperty(value="存储源 key")
	private String storageKey;

	@ApiModelProperty(value="短链 key")
	private String shortKey;

	@ApiModelProperty(value="访问时间从")
	private String dateFrom;

	@ApiModelProperty(value="访问时间至")
	private String dateTo;

	@ApiModelProperty(value="访问 ip")
	private String ip;

	@ApiModelProperty(value="访问 user_agent")
	private String userAgent;

	@ApiModelProperty(value="访问 referer")
	private String referer;

	@NotEmpty(message = "分页页数不能为空")
	private Integer page;

	@NotEmpty(message = "每页条数不能为空")
	private Integer limit;

}