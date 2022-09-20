package im.zhaojun.zfile.module.storage.model.param;

import im.zhaojun.zfile.module.storage.annotation.StorageParamItem;
import lombok.Getter;

/**
 * MinIO 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class MinIOParam extends S3BaseParam {

	@StorageParamItem(name = "Bucket 域名 / CDN 加速域名", required = false, order = 5, description = "为 minio 的服务地址，非 web 访问地址，一般为 http://ip:9000")
	private String domain;
	
	@StorageParamItem(name = "地域", defaultValue = "auto")
	private String region;

	@StorageParamItem(name = "服务地址", order = 5, description = "为 minio 的服务地址，非 web 访问地址，一般为 http://ip:9000")
	private String endPoint;

}