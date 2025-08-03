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
 * @author zhaojun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryShortLinkLogRequest extends PageQueryRequest {
	
	@Schema(title="短链 key")
	private String key;
	
	@Schema(title="存储源 id")
	private String storageId;
	
	@Schema(title="短链文件路径")
	private String url;

	@Schema(title="访问时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private List<Date> searchDate;

	public Date getDateFrom() {
		if (searchDate == null) {
			return null;
		}
		return DateUtil.beginOfDay(searchDate.getFirst());
	}

	public Date getDateTo() {
		if (searchDate == null) {
			return null;
		}
		return DateUtil.endOfDay(searchDate.getLast());
	}
}