package im.zhaojun.zfile.module.storage.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 检查文件夹密码规则的注解, 判断是否有权限访问文件夹
 *
 * @author zhaojun
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPassword {
	
	/**
	 * 存储源 key 字段表达式
	 */
	String storageKeyFieldExpression();
	
	/**
	 * 路径字段名称
	 */
	String pathFieldExpression();
	
	/**
	 * 密码字段名称
	 */
	String passwordFieldExpression();
	
	/**
	 * 路径是否是文件夹, 如果为 false, 则会取路径的父目录作为路径
	 */
	boolean pathIsDirectory() default true;
	
}