package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 文件列表信息结果类
 *
 * @author zhaojun
 */
@Data
@Schema(title="文件列表信息结果类")
@AllArgsConstructor
public class FileInfoResult {

	@Schema(title="文件列表")
	private List<FileItemResult> files;

	@Schema(title="当前目录密码路径表达式")
	private String passwordPattern;

}