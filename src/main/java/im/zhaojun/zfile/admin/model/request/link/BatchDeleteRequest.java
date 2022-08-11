package im.zhaojun.zfile.admin.model.request.link;

import lombok.Data;

import java.util.List;

/**
 * @author zhaojun
 */
@Data
public class BatchDeleteRequest {
	
	private List<Integer> ids;
	
}