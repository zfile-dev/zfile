package im.zhaojun.zfile.module.storage.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取 S3 bucket 列表请求类
 *
 * @author zhaojun
 */
@Data
@ApiModel(value="S3 bucket 列表请求类")
public class GetS3BucketListRequest {

	@NotBlank(message = "accessKey 不能为空")
	@ApiModelProperty(value = "accessKey", required = true, example = "XQEWQJI129JAS12")
	private String accessKey;

	@NotBlank(message = "secretKey 不能为空")
	@ApiModelProperty(value = "secretKey", required = true, example = "EWQJI129JAS11AE2")
	private String secretKey;

	@NotBlank(message = "EndPoint 不能为空")
	@ApiModelProperty(value = "Endpoint 接入点", required = true, example = "oss-cn-beijing.aliyuncs.com")
	private String endPoint;

	@ApiModelProperty(value = "Endpoint 接入点", required = true, example = "cn-beijing")
	private String region;

}