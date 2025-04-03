package im.zhaojun.zfile.core.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用分页请求对象，可继承该类增加业务字段.
 *
 * @author zhaojun
 */
@Data
public class PageQueryRequest {
	
	@Schema(name="分页页数")
	private Integer page = 1;
	
	@Schema(name="每页条数")
	private Integer limit = 10;
	
	@Schema(name="排序字段")
	private String orderBy = "create_date";
	
	@Schema(name="排序顺序")
	private String orderDirection = "desc";
	
}