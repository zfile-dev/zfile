package im.zhaojun.zfile.module.storage.annotation;

import im.zhaojun.zfile.module.storage.enums.StorageParamItemAnnoEnum;
import im.zhaojun.zfile.module.storage.model.enums.StorageParamTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记存储类型参数名称
 *
 * @author zhaojun
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StorageParamItem {

	/**
	 * 是否是捐赠版功能.
	 */
	boolean pro() default false;

	/**
	 * 字段显示排序值, 值越小, 越靠前. 默认为 99
	 */
	int order() default 99;

	/**
	 * 参数键, 如果为空, 则使用字段名称.
	 */
	String key() default "";

	/**
	 * 参数名称, 用于网页上显示名称.
	 */
	String name() default "";

	/**
	 * 字段类型, 默认为 input, 可选值为: input, textarea, select, switch.
	 */
	StorageParamTypeEnum type() default StorageParamTypeEnum.INPUT;

	/**
	 * 当 {@link #type} 为 select 时, 选项的值.
	 */
	StorageParamSelectOption[] options() default {};

	/**
	 * 当 {@link #type} 为 select 时, 选项的值. 通过 {@link StorageParamSelect#getOptions)} 方法获取选项值.
	 */
	Class<? extends StorageParamSelect> optionsClass() default StorageParamSelect.class;

	/**
	 * 当 {@link #type} 为 select 时, 是否允许用户创建选项.
	 */
	boolean optionAllowCreate() default false;

	/**
	 * 参数值是否可以为空. 如不为空，则抛出异常.
	 */
	boolean required() default true;

	/**
	 * 如果填写值为空，则给予默认值.
	 * 支持 ${xxx} 变量, 会读取配置文件中的值, 如获取失败, 会默认为空.
	 */
	String defaultValue() default "";

	/**
	 * 参数描述信息, 用户在用户填写时, 进行提示.
	 */
	String description() default "";

	/**
	 * 参数下方的提示链接, 如果为空, 则不显示.
	 */
	String link() default "";

	/**
	 * 参数下方的提示链接文件信息, 如果为空, 则默认为链接地址.
	 */
	String linkName() default "";

	/**
	 * 是否忽略参数不传递给前端，也不保存到数据库，一般是临时参数
	 */
	boolean ignoreInput() default false;

	/**
	 * 是否前端不显示该字段.
	 */
	boolean hidden() default false;

	/**
	 * 判断条件表达式，表达式结果为 true 时才显示该字段
	 */
	String condition() default "";

	/**
	 * 为了简略子类的注解只修改父类注解某些字段的情况, 直接全部复制的话，后期维护困难，容易不同步, 可以使用该字段描述哪些字段以子类的值为准，其他的从父类继承.
	 */
	StorageParamItemAnnoEnum[] onlyOverwrite() default {};

}