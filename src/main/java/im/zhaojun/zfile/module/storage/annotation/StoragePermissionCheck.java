package im.zhaojun.zfile.module.storage.annotation;

import im.zhaojun.zfile.module.storage.model.enums.FileOperatorTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 存储源权限检查
 *
 * @author zhaojun
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StoragePermissionCheck {

	/**
	 * 文件操作类型枚举
	 */
	FileOperatorTypeEnum action();

}