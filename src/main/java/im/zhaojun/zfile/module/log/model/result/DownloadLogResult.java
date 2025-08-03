package im.zhaojun.zfile.module.log.model.result;

import im.zhaojun.zfile.module.storage.model.enums.StorageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 下载日志结果类
 *
 * @author zhaojun
 */
@Data
public class DownloadLogResult {

	@Schema(title="")
	private Integer id;

	@Schema(title="文件路径")
	private String path;

	@Schema(title = "存储源类型")
	private StorageTypeEnum storageType;
	
	@Schema(title = "存储源名称", example = "我的本地存储")
	private String storageName;

	@Schema(title = "存储源Key", example = "local")
	private String storageKey;

	@Schema(title="访问时间")
	private Date createTime;

	@Schema(title="访问 ip")
	private String ip;
	
	@Schema(title = "短链 Key")
	private String shortKey;

	@Schema(title="访问 user_agent")
	private String userAgent;

	@Schema(title="访问 referer")
	private String referer;

	@Schema(title="短链地址")
	private String shortLink;

	@Schema(title="直链地址")
	private String pathLink;

}