package im.zhaojun.zfile.core.model.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class PageQueryRequest {
	
	@ApiModelProperty(value="分页页数")
	private Integer page = 1;
	
	@ApiModelProperty(value="每页条数")
	private Integer limit = 10;
	
	@ApiModelProperty(value="排序字段")
	private String orderBy = "create_date";
	
	@ApiModelProperty(value="排序顺序")
	private String orderDirection = "desc";
	
}