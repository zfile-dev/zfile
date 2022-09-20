package im.zhaojun.zfile.module.storage.model.request.base;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.core.util.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取指定文件信息的请求参数
 *
 * @author zhaojun
 */
@Data
@ApiModel(description = "获取指定文件信息的请求类")
public class FileItemRequest {

    @ApiModelProperty(value = "存储源 key", required = true, example = "local")
    @NotBlank(message = "存储源 key 不能为空")
    private String storageKey;

    @ApiModelProperty(value = "请求路径", example = "/")
    private String path;

	@ApiModelProperty(value = "文件夹密码, 如果文件夹需要密码才能访问，则支持请求密码", example = "123456")
	private String password;

    public void handleDefaultValue() {
        if (StrUtil.isEmpty(path)) {
            path = "/";
        }
		// 自动补全路径, 如 a 补全为 /a/
        path = StringUtils.concat(path);
    }

}