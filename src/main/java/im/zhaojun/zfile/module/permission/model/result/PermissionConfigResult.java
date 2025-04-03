package im.zhaojun.zfile.module.permission.model.result;

import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;
import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class PermissionConfigResult {

	/**
	 * 操作
	 */
	private FileOperatorTypeEnum operator;

	/**
	 * 允许管理员操作
	 */
	private Boolean allowAdmin;

	/**
	 * 允许匿名用户操作
	 */
	private Boolean allowAnonymous;

	/**
	 * 存储源 ID
	 */
	private String operatorName;

	/**
	 * 提示信息
	 */
	private String tips;

}