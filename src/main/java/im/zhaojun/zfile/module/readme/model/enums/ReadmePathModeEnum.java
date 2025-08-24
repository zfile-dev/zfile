package im.zhaojun.zfile.module.readme.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Readme 路径模式枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum ReadmePathModeEnum {

	/**
	 * 相对路径
	 */
    RELATIVE("relative"),

	/**
	 * 绝对路径
	 */
    ABSOLUTE("absolute");

	@EnumValue
	@JsonValue
	private final String value;

}