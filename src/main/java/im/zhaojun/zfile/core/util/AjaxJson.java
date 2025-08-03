package im.zhaojun.zfile.core.util;

import im.zhaojun.zfile.core.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * ajax 请求返回 JSON 格式数据的封装
 *
 * @author zhaojun
 */
@Data
@ToString
public class AjaxJson<T> implements Serializable {

	private static final long serialVersionUID = 1L;    // 序列化版本号

	public static final String CODE_SUCCESS = "0";            // 成功状态码

	@Schema(title = "业务状态码，0 为正常，其他值均为异常，异常情况下见响应消息", example = "0")
	private final String code;

	@Schema(title = "响应消息", example = "ok")
	private String msg;

	@Schema(title = "响应数据")
	private T data;

	@Schema(title = "数据总条数，分页情况有效")
	private final Long dataCount;
	
	@Schema(title = "跟踪 ID")
	private String traceId;

	public AjaxJson(String code, String msg) {
		if (code == null) {
			code = ErrorCode.SYSTEM_ERROR.getCode();
		}
		this.code = code;
		this.msg = msg;
		this.dataCount = null;
	}

	public AjaxJson(String code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.dataCount = null;
	}

	public AjaxJson(String code, String msg, T data, Long dataCount) {
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.dataCount = dataCount;
	}

	// 返回成功
	public static AjaxJson<Void> getSuccess() {
		return new AjaxJson<>(CODE_SUCCESS, "ok");
	}

	public static AjaxJson<Void> getSuccess(String msg) {
		return new AjaxJson<>(CODE_SUCCESS, msg);
	}

	public static <T> AjaxJson<T> getSuccess(String msg, T data) {
		return new AjaxJson<>(CODE_SUCCESS, msg, data);
	}

	public static <T> AjaxJson<T> getSuccessData(T data) {
		return new AjaxJson<>(CODE_SUCCESS, "ok", data);
	}

	// 返回分页和数据的
	public static <T> AjaxJson<T> getPageData(Long dataCount, T data) {
		return new AjaxJson<>(CODE_SUCCESS, "ok", data, dataCount);
	}

	// 返回错误
	public static AjaxJson<Void> getError(String msg) {
		return new AjaxJson<>(ErrorCode.SYSTEM_ERROR.getCode(), msg);
	}

	// 返回未登录
	public static AjaxJson<?> getUnauthorizedResult() {
		return new AjaxJson<>(ErrorCode.BIZ_UNAUTHORIZED.getCode(), "未登录，请登录后再次访问");
	}

	// 返回没权限的
	public static AjaxJson<?> getForbiddenResult() {
		return new AjaxJson<>(ErrorCode.NO_FORBIDDEN.getCode(), "未授权，请登录正确权限账号再试");
	}

	// 返回未找到的
	public static AjaxJson<?> getNotFoundResult() {
		return new AjaxJson<>(ErrorCode.BIZ_NOT_FOUND.getCode(), ErrorCode.BIZ_NOT_FOUND.getMessage());
	}

	public static AjaxJson<?> getError(String code, String msg) {
		return new AjaxJson<>(code, msg);
	}

}