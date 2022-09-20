package im.zhaojun.zfile.module.storage.model.request.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取文件夹参数请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "获取文件夹参数请求类")
public class FileListConfigRequest {

	@ApiModelProperty(value = "存储源 key", required = true, example = "local")
	@NotBlank(message = "存储源 key 不能为空")
	private String storageKey;

	@ApiModelProperty(value = "请求路径", example = "/")
	private String path = "/";

}