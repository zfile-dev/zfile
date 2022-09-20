package im.zhaojun.zfile.module.storage.controller.helper;

import cn.hutool.core.util.StrUtil;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.module.storage.model.request.GetS3BucketListRequest;
import im.zhaojun.zfile.module.storage.model.result.S3BucketNameResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * S3 工具辅助
 *
 * @author zhaojun
 */
@Api(tags = "S3 工具辅助模块")
@Controller
@RequestMapping("/s3")
public class S3HelperController {

	@PostMapping("/getBuckets")
	@ResponseBody
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取 S3 存储器列表")
	public AjaxJson<List<S3BucketNameResult>> getBucketNames(@Valid @RequestBody GetS3BucketListRequest getS3BucketListRequest) {
		List<S3BucketNameResult> bucketNameList = new ArrayList<>();
		String accessKey = getS3BucketListRequest.getAccessKey();
		String secretKey = getS3BucketListRequest.getSecretKey();
		String endPoint = getS3BucketListRequest.getEndPoint();
		String region = getS3BucketListRequest.getRegion();

		if (StrUtil.isEmpty(region) && StrUtil.contains(endPoint, '.')) {
			region = endPoint.split("\\.")[1];
		}

		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

		List<Bucket> buckets = amazonS3.listBuckets();
		for (Bucket bucket : buckets) {
			S3BucketNameResult s3BucketNameResult = new S3BucketNameResult(bucket.getName(), bucket.getCreationDate());
			bucketNameList.add(s3BucketNameResult);
		}

		return AjaxJson.getSuccessData(bucketNameList);
	}

}