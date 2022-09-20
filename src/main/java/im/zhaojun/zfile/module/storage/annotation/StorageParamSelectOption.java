package im.zhaojun.zfile.module.storage.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记存储类型参数类型为 select 时, 数据的下拉值.
 *
 * @author zhaojun
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageParamSelectOption {

	/**
	 * 选项显示值
	 */
	String label();

	/**
	 * 选项存储值
	 */
	String value();

}