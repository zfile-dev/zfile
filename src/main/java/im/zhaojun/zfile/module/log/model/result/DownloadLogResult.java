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

	@Schema(name="")
	private Integer id;

	@Schema(name="文件路径")
	private String path;

	@Schema(name = "存储源类型")
	private StorageTypeEnum storageType;
	
	@Schema(name = "存储源名称", example = "我的本地存储")
	private String storageName;

	@Schema(name = "存储源Key", example = "local")
	private String storageKey;

	@Schema(name="访问时间")
	private Date createTime;

	@Schema(name="访问 ip")
	private String ip;
	
	@Schema(name = "短链 Key")
	private String shortKey;

	@Schema(name="访问 user_agent")
	private String userAgent;

	@Schema(name="访问 referer")
	private String referer;

}