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
public class QueryLoginLogRequest extends PageQueryRequest {

	@Schema(title="用户名")
	private String username;

	@Schema(title="密码")
	private String password;

	@Schema(title="IP")
	private String ip;

	@Schema(title="User-Agent")
	private String userAgent;

	@Schema(title="来源")
	private String referer;

	@Schema(title="登录结果")
	private String result;

	@Schema(title="访问时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private List<Date> searchDate;

	@Schema(title="排序字段")
	private String orderBy = "create_time";

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