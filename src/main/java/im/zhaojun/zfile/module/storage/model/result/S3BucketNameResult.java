package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * S3 bucket 名称结果类
 * 
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@ApiModel(value="S3 bucket 名称结果类")
public class S3BucketNameResult {

	@ApiModelProperty(value = "bucket 名称", example = "zfile")
	private String name;

	@ApiModelProperty(value = "bucket 创建时间", example = "2022-01-01 15:22")
	private Date date;

}