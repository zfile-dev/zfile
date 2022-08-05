package im.zhaojun.zfile.admin.model.param;

import im.zhaojun.zfile.admin.annotation.StorageParamItem;
import lombok.Getter;

/**
 * MinIO 初始化参数
 *
 * @author zhaojun
 */
@Getter
public class MinIOParam extends S3BaseParam {

	@StorageParamItem(name = "地域")
	private String region;

	@StorageParamItem(name = "服务地址", description = "为 minio 的服务地址，非 web 访问地址，一般为 http://ip:9000")
	private String endPoint;

}