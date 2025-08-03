package im.zhaojun.zfile.module.storage.model.request.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取文件夹参数请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "获取文件夹参数请求类")
public class FileListConfigRequest {

	@Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
	@NotBlank(message = "存储源 key 不能为空")
	private String storageKey;

	@Schema(title = "请求路径", example = "/")
	private String path = "/";
	
	@Schema(title = "文件夹密码", example = "123456")
	private String password;

}