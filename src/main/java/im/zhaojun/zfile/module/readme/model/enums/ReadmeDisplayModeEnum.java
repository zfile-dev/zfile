package im.zhaojun.zfile.module.readme.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Readme 展示模式枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum ReadmeDisplayModeEnum {

	/**
	 * 顶部显示
	 */
	TOP("top"),

	/**
	 * 底部显示
	 */
	BOTTOM("bottom"),

	/**
	 * 弹窗显示
	 */
	DIALOG("dialog");

	@EnumValue
	@JsonValue
	private final String value;

}