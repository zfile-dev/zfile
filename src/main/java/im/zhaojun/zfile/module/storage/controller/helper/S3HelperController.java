package im.zhaojun.zfile.module.storage.controller.helper;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.util.AjaxJson;
import im.zhaojun.zfile.core.util.StringUtils;
import im.zhaojun.zfile.core.util.UrlUtils;
import im.zhaojun.zfile.module.storage.model.dto.ZFileCORSRule;
import im.zhaojun.zfile.module.storage.model.request.GetS3BucketListRequest;
import im.zhaojun.zfile.module.storage.model.request.GetS3CorsListRequest;
import im.zhaojun.zfile.module.storage.model.result.S3BucketNameResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.GetBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * S3 工具辅助
 *
 * @author zhaojun
 */
@Tag(name = "S3 工具辅助模块")
@Controller
@RequestMapping("/s3")
public class S3HelperController {

	@PostMapping("/getBuckets")
	@ResponseBody
	@ApiOperationSupport(order = 1)
	@Operation(summary = "获取 S3 存储器列表")
	public AjaxJson<List<S3BucketNameResult>> getBucketNames(@Valid @RequestBody GetS3BucketListRequest getS3BucketListRequest) {
		List<S3BucketNameResult> bucketNameList = new ArrayList<>();
		String accessKey = getS3BucketListRequest.getAccessKey();
		String secretKey = getS3BucketListRequest.getSecretKey();
		String endPoint = getS3BucketListRequest.getEndPoint();
		if (!UrlUtils.hasScheme(endPoint)) {
			endPoint = "http://" + endPoint;
		}
		String region = getS3BucketListRequest.getRegion();

		if (StringUtils.isEmpty(region) && StringUtils.contains(endPoint, StringUtils.DOT)) {
			region = endPoint.split("\\.")[1];
		}

		if (StringUtils.isEmpty(region)) {
			region = "us-east-1";
		}

		List<Bucket> buckets;
		S3Client s3Client = null;
		try {
			Region oss = Region.of(region);
			URI endpointOverride = URI.create(endPoint);
			StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

			s3Client = S3Client.builder()
					.region(oss)
					.endpointOverride(endpointOverride)
					.credentialsProvider(credentialsProvider)
					.build();
			buckets = s3Client.listBuckets().buckets();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SystemException("S3 工具辅助模块获取 Bucket 列表失败", e);
		} finally {
			if (s3Client != null) {
				try {
					s3Client.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

		for (Bucket bucket : buckets) {
			S3BucketNameResult s3BucketNameResult = new S3BucketNameResult(bucket.name(), Date.from(bucket.creationDate()));
			bucketNameList.add(s3BucketNameResult);
		}

		return AjaxJson.getSuccessData(bucketNameList);
	}

	@PostMapping("/getCorsConfig")
	@ResponseBody
	@ApiOperationSupport(order = 1)
	@Operation(summary = "获取 S3 跨域设置")
	public AjaxJson<List<ZFileCORSRule>> getCorsConfig(@Valid @RequestBody GetS3CorsListRequest getS3CorsListRequest) {
		String accessKey = getS3CorsListRequest.getAccessKey();
		String secretKey = getS3CorsListRequest.getSecretKey();
		String endPoint = getS3CorsListRequest.getEndPoint();
		if (!UrlUtils.hasScheme(endPoint)) {
			endPoint = "http://" + endPoint;
		}
		String region = getS3CorsListRequest.getRegion();
		String bucketName = getS3CorsListRequest.getBucketName();

		if (StringUtils.isEmpty(region) && StringUtils.contains(endPoint, StringUtils.DOT)) {
			region = endPoint.split("\\.")[1];
		}

		if (StringUtils.isEmpty(region)) {
			region = "us-east-1";
		}

		S3Client s3Client = null;
		try {
			Region oss = Region.of(region);
			URI endpointOverride = URI.create(endPoint);
			StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

			s3Client = S3Client.builder()
					.region(oss)
					.endpointOverride(endpointOverride)
					.credentialsProvider(credentialsProvider)
					.build();
			GetBucketCorsResponse getBucketCorsResponse = s3Client.getBucketCors(builder -> builder.bucket(bucketName));
			List<CORSRule> rules = getBucketCorsResponse.corsRules();
			List<ZFileCORSRule> rulesList = ZFileCORSRule.fromCORSRule(rules);
			return AjaxJson.getSuccessData(rulesList);
		} catch (S3Exception s3Exception) {
			if (s3Exception.statusCode() == 404) {
				return AjaxJson.getSuccessData(Collections.emptyList());
			} else {
				throw new SystemException("获取跨域设置失败: " + s3Exception.getMessage(), s3Exception);
			}
		} catch (Exception e) {
			throw new SystemException("自动获取跨域设置失败: " + e.getMessage(), e);
		} finally {
			if (s3Client != null) {
				try {
					s3Client.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}

	}

}