package im.zhaojun.zfile.module.config.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件点击模式枚举, 用于控制文件是单击打开还是双击打开
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum FileClickModeEnum {

	/**
	 * 单击打开文件/文件夹
	 */
	CLICK("click"),

	/**
	 * 双击打开文件/文件夹
	 */
	DBCLICK("dbclick");

	@EnumValue
	@JsonValue
	private final String value;

}