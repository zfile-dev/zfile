package im.zhaojun.zfile.module.link.model.request;

import im.zhaojun.zfile.core.model.request.PageQueryRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhaojun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryShortLinkLogRequest extends PageQueryRequest {
	
	@ApiModelProperty(value="短链 key")
	private String key;
	
	@ApiModelProperty(value="存储源 id")
	private String storageId;
	
	@ApiModelProperty(value="短链文件路径")
	private String url;
	
	@ApiModelProperty(value="访问时间从")
	private String dateFrom;
	
	@ApiModelProperty(value="访问时间至")
	private String dateTo;
	
}