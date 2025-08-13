package im.zhaojun.zfile.module.link.model.request;

import cn.hutool.core.date.DateUtil;
import im.zhaojun.zfile.core.model.request.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 查询下载日志请求参数
 *
 * @author zhaojun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDownloadLogRequest extends PageQueryRequest {

	@Schema(title="文件路径")
	private String path;

	@Schema(title="存储源 key")
	private String storageKey;

	@Schema(title="链接类型")
	private String linkType;

	@Schema(title="短链 key")
	private String shortKey;

	@Schema(title="访问时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private List<Date> searchDate;

	@Schema(title="访问 ip")
	private String ip;

	@Schema(title="访问 user_agent")
	private String userAgent;

	@Schema(title="访问 referer")
	private String referer;
	
	@Schema(title="排序字段")
	private String orderBy = "create_time";

	public Date getDateFrom() {
		if (searchDate == null || searchDate.isEmpty()) {
			return null;
		}
		return DateUtil.beginOfDay(searchDate.getFirst());
	}

	public Date getDateTo() {
		if (searchDate == null || searchDate.isEmpty()) {
			return null;
		}
		return DateUtil.endOfDay(searchDate.getLast());
	}

}