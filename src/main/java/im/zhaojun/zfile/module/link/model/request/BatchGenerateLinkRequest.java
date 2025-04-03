package im.zhaojun.zfile.module.link.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量生成直链请求类
 * @author zhaojun
 */
@Data
@Schema(description = "批量生成直链请求类")
public class BatchGenerateLinkRequest {
	
	@NotBlank(message = "存储源 key 不能为空")
	private String storageKey;
	
	@NotEmpty(message = "生成的文件路径不能为空")
	private List<String> paths;

	/**
	 * 有效期, 单位: 秒
	 */
	@NotNull(message = "过期时间不能为空")
	private Long expireTime;
}