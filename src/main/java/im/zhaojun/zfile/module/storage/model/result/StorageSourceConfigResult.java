package im.zhaojun.zfile.module.storage.model.result;

import im.zhaojun.zfile.module.readme.model.enums.ReadmeDisplayModeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 存储源设置响应类
 *
 * @author zhaojun
 */
@ApiModel(value="存储源设置响应类")
@Data
public class StorageSourceConfigResult {

	@ApiModelProperty(value = "是否启用文件操作功能", example = "true", notes = "是否启用文件上传，编辑，删除等操作.")
	private Boolean enableFileOperator;

	@ApiModelProperty(value="readme 文本内容, 支持 md 语法.")
	private String readmeText;

	@ApiModelProperty(value = "显示模式", required = true, example = "readme 显示模式，支持顶部显示: top, 底部显示:bottom, 弹窗显示: dialog")
	private ReadmeDisplayModeEnum readmeDisplayMode;

	@ApiModelProperty(value = "是否默认开启图片模式", example = "true")
	private Boolean defaultSwitchToImgMode;

}