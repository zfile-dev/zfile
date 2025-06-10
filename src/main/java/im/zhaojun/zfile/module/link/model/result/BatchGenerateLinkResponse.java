package im.zhaojun.zfile.module.link.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
@Schema(description = "批量生成直链结果类")
@AllArgsConstructor
public class BatchGenerateLinkResponse {
	
	private String address;

}