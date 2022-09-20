package im.zhaojun.zfile.module.link.model.request;

import lombok.Data;

import java.util.List;

/**
 * @author zhaojun
 */
@Data
public class BatchDeleteRequest {
	
	private List<Integer> ids;
	
}