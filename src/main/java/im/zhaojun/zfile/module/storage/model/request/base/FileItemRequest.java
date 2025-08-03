package im.zhaojun.zfile.module.storage.model.request.base;

import im.zhaojun.zfile.core.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 获取指定文件信息的请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "获取指定文件信息的请求类")
public class FileItemRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "请求路径", example = "/")
    private String path;

	@Schema(title = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
	private String password;

    public void handleDefaultValue() {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
		// 自动补全路径, 如 a 补全为 /a/
        path = StringUtils.concat(path);
    }

}