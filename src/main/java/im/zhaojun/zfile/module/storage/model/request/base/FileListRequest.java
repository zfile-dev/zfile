package im.zhaojun.zfile.module.storage.model.request.base;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.validation.StringListValue;
import im.zhaojun.zfile.core.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 获取文件夹下文件列表请求参数
 *
 * @author zhaojun
 */
@Data
@Schema(description = "获取文件夹下文件列表请求类")
public class FileListRequest {

    @Schema(title = "存储源 key", requiredMode = Schema.RequiredMode.REQUIRED, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @Schema(title = "请求路径", example = "/")
    private String path;

	@Schema(title = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
	private String password;

	@StringListValue(message = "排序字段参数异常，只能是 name、size、time", vals = {"name", "size", "time"})
	private String orderBy;

	@StringListValue(message = "排序顺序参数异常，只能是 asc 或 desc", vals = {"asc", "desc"})
	private String orderDirection;

    public void handleDefaultValue() {
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }
    	if (StringUtils.isEmpty(orderBy)) {
    		orderBy = "name";
    	}
        if (StringUtils.isEmpty(orderDirection)) {
    		orderDirection = "asc";
    	}

		// 自动补全路径, 如 a 补全为 /a/
        path = StringUtils.concat(path);
    }

}