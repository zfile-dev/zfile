package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * gd drive 基本信息结果类
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@ApiModel(value="gd drive 基本信息结果类")
public class GoogleDriveInfoResult {
	
	@ApiModelProperty(value = "drive id", example = "0AGrY0xF1D7PEUk9PVB")
	private String id;
	
	@ApiModelProperty(value = "drive 名称", example = "zfile")
	private String name;
	
}