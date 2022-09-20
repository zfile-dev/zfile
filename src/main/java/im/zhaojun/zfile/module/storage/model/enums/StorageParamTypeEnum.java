package im.zhaojun.zfile.module.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储源参数类型枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum StorageParamTypeEnum {

	/**
	 * 输入框
	 */
	INPUT("input"),

	/**
	 * 下拉框
	 */
	SELECT("select"),

	/**
	 * 开关
	 */
	SWITCH("switch");

	@EnumValue
	@JsonValue
	private final String value;

}