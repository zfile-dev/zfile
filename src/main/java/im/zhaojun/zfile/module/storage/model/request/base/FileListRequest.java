package im.zhaojun.zfile.module.storage.model.request.base;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.validation.StringListValue;
import im.zhaojun.zfile.core.util.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取文件夹下文件列表请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "获取文件夹下文件列表请求类")
public class FileListRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "请求路径", example = "/")
    private String path;

	@ApiModelProperty(value = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
	private String password;

	@StringListValue(message = "排序字段参数异常，只能是 name、size、time", vals = {"name", "size", "time"})
	private String orderBy;

	@StringListValue(message = "排序顺序参数异常，只能是 asc 或 desc", vals = {"asc", "desc"})
	private String orderDirection;

    public void handleDefaultValue() {
        if (StrUtil.isEmpty(path)) {
            path = "/";
        }
    	if (StrUtil.isEmpty(orderBy)) {
    		orderBy = "name";
    	}
        if (StrUtil.isEmpty(orderDirection)) {
    		orderDirection = "asc";
    	}

		// 自动补全路径, 如 a 补全为 /a/
        path = StringUtils.concat(path);
    }

}