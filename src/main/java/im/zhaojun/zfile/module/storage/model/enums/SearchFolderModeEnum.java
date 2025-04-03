package im.zhaojun.zfile.module.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 搜索模式枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum SearchFolderModeEnum {

	/**
	 * 搜索当前文件夹
	 */
	SEARCH_CURRENT_FOLDER("search_current_folder"),

	/**
	 * 当前文件夹及所有子文件夹
	 */
	SEARCH_CURRENT_FOLDER_AND_CHILD("search_current_folder_and_child"),

	/**
	 * 当前所有文件夹
	 */
	SEARCH_ALL("search_all");

	@EnumValue
	@JsonValue
	private final String value;

}