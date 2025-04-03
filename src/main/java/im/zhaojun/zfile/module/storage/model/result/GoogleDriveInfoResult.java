package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * gd drive 基本信息结果类
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@Schema(name="gd drive 基本信息结果类")
public class GoogleDriveInfoResult {
	
	@Schema(name = "drive id", example = "0AGrY0xF1D7PEUk9PVB")
	private String id;
	
	@Schema(name = "drive 名称", example = "zfile")
	private String name;
	
}