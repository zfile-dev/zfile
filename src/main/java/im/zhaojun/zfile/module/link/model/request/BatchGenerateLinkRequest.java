package im.zhaojun.zfile.module.link.model.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量生成直链请求类
 * @author zhaojun
 */
@Data
@ApiModel(description = "批量生成直链请求类")
public class BatchGenerateLinkRequest {
	
	@NotBlank(message = "存储源 key 不能为空")
	private String storageKey;
	
	@NotEmpty(message = "生成的文件路径不能为空")
	private List<String> paths;
	
}