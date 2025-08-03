package im.zhaojun.zfile.module.storage.model.result;

import im.zhaojun.zfile.module.readme.model.enums.ReadmeDisplayModeEnum;
import im.zhaojun.zfile.module.storage.model.bo.StorageSourceMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 存储源设置响应类
 *
 * @author zhaojun
 */
@Schema(title="存储源设置响应类")
@Data
public class StorageSourceConfigResult {

	@Schema(title="readme 文本内容, 支持 md 语法.")
	private String readmeText;

	@Schema(title = "显示模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "readme 显示模式，支持顶部显示: top, 底部显示:bottom, 弹窗显示: dialog")
	private ReadmeDisplayModeEnum readmeDisplayMode;

	@Schema(title = "是否默认开启图片模式", example = "true")
	private Boolean defaultSwitchToImgMode;

	private Map<String, Boolean> permission;

	@Schema(title = "存储源元数据")
	private StorageSourceMetadata metadata;

	private String rootPath;

}