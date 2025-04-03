package im.zhaojun.zfile.module.storage.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name="S3 bucket 名称结果类")
public class S3BucketNameResult {

	@Schema(name = "bucket 名称", example = "zfile")
	private String name;

	@Schema(name = "bucket 创建时间", example = "2022-01-01 15:22")
	private Date date;

}