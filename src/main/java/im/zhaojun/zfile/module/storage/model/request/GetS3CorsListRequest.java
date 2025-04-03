package im.zhaojun.zfile.module.storage.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取 S3 bucket 列表请求类
 *
 * @author zhaojun
 */
@Data
@Schema(name="S3 bucket 列表请求类")
public class GetS3CorsListRequest {

	@NotBlank(message = "accessKey 不能为空")
	@Schema(name = "accessKey", requiredMode = Schema.RequiredMode.REQUIRED, example = "XQEWQJI129JAS12")
	private String accessKey;

	@NotBlank(message = "secretKey 不能为空")
	@Schema(name = "secretKey", requiredMode = Schema.RequiredMode.REQUIRED, example = "EWQJI129JAS11AE2")
	private String secretKey;

	@NotBlank(message = "EndPoint 不能为空")
	@Schema(name = "Endpoint 接入点", requiredMode = Schema.RequiredMode.REQUIRED, example = "oss-cn-beijing.aliyuncs.com")
	private String endPoint;

	@Schema(name = "Endpoint 接入点", requiredMode = Schema.RequiredMode.REQUIRED, example = "cn-beijing")
	private String region;

	@Schema(name = "存储桶名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "zfile-bucket")
	private String bucketName;

}