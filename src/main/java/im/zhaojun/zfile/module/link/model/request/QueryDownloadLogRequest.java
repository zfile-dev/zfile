package im.zhaojun.zfile.module.link.model.request;

import im.zhaojun.zfile.core.model.request.PageQueryRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询下载日志请求参数
 *
 * @author zhaojun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDownloadLogRequest extends PageQueryRequest {

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
	
	@ApiModelProperty(value="排序字段")
	private String orderBy = "create_time";
	
}