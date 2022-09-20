package im.zhaojun.zfile.module.storage.model.bo;

import im.zhaojun.zfile.module.storage.annotation.StorageParamSelectOption;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * 存储源参数定义, 包含参数名称、描述、必填、默认值等信息.
 *
 * @author zhaojun
 */
@Data
@AllArgsConstructor
@Builder
public class StorageSourceParamDef {

	/**
	 * 字段显示排序值, 值越小, 越靠前.
	 */
	private int order;

	/**
	 * 参数 key
	 */
	private String key;

	/**
	 * 参数名称
	 */
	private String name;

	/**
	 * 参数描述
	 */
	private String description;

	/**
	 * 是否必填
	 */
	private boolean required;

	/**
	 * 默认值
	 */
	private String defaultValue;

	/**
	 * 链接地址
	 */
	private String link;

	/**
	 * 链接名称
	 */
	private String linkName;

	/**
	 * 字段类型, 默认为 input, 可选值为: input, select, switch.
	 */
	private StorageParamTypeEnum type;

	/**
	 * 当 {@link #type} 为 select 时, 选项的值.
	 */
	private List<Options> options;

	@Getter
	public static class Options {

		private final String label;

		private final String value;

		public Options(String value) {
			this.label = value;
			this.value = value;
		}

		public Options(String label, String value) {
			this.label = label;
			this.value = value;
		}
		public Options(StorageParamSelectOption storageParamSelectOption) {
			this.label = storageParamSelectOption.label();
			this.value = storageParamSelectOption.value();
		}

	}

}