package im.zhaojun.zfile.module.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件操作类型枚举
 *
 * @author zhaojun
 */
@Getter
@AllArgsConstructor
public enum FileOperatorTypeEnum {
	
	/**
	 * 获取文件上传链接操作
	 */
	UPLOAD("上传", "upload"),
	
	/**
	 * 新建文件夹操作
	 */
	NEW_FOLDER("新建文件夹", "new_folder"),
	
	/**
	 * 删除文件&文件夹操作
	 */
	DELETE("删除", "delete"),
	
	/**
	 * 重命名文件&文件夹操作
	 */
	RENAME("重命名", "rename"),
	
	/**
	 * 复制文件&文件夹操作
	 */
	COPY("复制", "copy"),
	
	/**
	 * 移动文件&文件夹操作
	 */
	MOVE("移动", "move");
	
	private final String name;
	
	@EnumValue
	@JsonValue
	private final String value;
	
}