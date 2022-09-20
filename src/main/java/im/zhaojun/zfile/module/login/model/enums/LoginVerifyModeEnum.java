package im.zhaojun.zfile.module.login.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登陆验证方式枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum LoginVerifyModeEnum {

	/**
	 * 不启用登陆模式
	 */
	OFF_MODE("off"),

	/**
	 * 图形验证码模式
	 */
	IMG_VERIFY_MODE("image"),

	/**
	 * 图形验证码模式
	 */
	TWO_FACTOR_AUTHENTICATION_MODE("2fa");

	@EnumValue
	@JsonValue
	private final String value;

}