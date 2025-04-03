package im.zhaojun.zfile.module.user.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登陆日志模式枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum LoginLogModeEnum {

	/**
	 * 不记录登录日志
	 */
	OFF("off"),

	/**
	 * 记录所有登录信息作为日志
	 */
	ALL("all"),

	/**
	 * 不在日志中记录登录成功的密码
	 */
	IGNORE_SUCCESS_PWD("ignoreSuccessPwd"),

	/**
	 * 不在日志中记录密码
	 */
	IGNORE_ALL_PWD("ignoreAllPwd");

	@EnumValue
	@JsonValue
	private final String value;

}