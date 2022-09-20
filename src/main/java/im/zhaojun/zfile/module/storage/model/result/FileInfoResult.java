package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 文件列表信息结果类
 *
 * @author zhaojun
 */
@Data
@ApiModel(value="文件列表信息结果类")
@AllArgsConstructor
public class FileInfoResult {

	@ApiModelProperty(value="文件列表")
	private List<FileItemResult> files;

	@ApiModelProperty(value="当前目录密码路径表达式")
	private String passwordPattern;

}