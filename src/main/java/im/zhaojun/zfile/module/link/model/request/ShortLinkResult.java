package im.zhaojun.zfile.module.link.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 短链结果类
 *
 * @author zhaojun
 */
@Data
public class ShortLinkResult {

	@Schema(title = "短链 id", example = "1")
	private Integer id;

	@Schema(title = "存储源名称", example = "我的本地存储")
	private String storageName;

	@Schema(title = "存储源类型")
	private StorageTypeEnum storageType;

	@JsonIgnore
	private String storageTypeStr;

	public String getStorageTypeStr() {
		return storageType.getDescription();
	}

	@Schema(title = "短链 key", example = "voldd3")
	private String shortKey;

	@Schema(title = "文件 url", example = "/directlink/1/test02.png")
	private String url;

	@Schema(title = "创建时间", example = "2021-11-22 10:05")
	private Date createDate;

	@Schema(title = "过期时间", example = "2021-11-23 10:05")
	private Date expireDate;

	@Schema(title="短链地址")
	private String shortLink;

}