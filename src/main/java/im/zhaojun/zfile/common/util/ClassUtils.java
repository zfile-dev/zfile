package im.zhaojun.zfile.common.util;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;

/**
 * Class & 反射相关工具类
 *
 * @author zhaojun
 */
public class ClassUtils {

	/**
	 * 获取指定类的泛型类型, 只获取第一个泛型类型
	 *
	 * @param   clazz
	 *          泛型类
	 *
	 * @return  泛型类型
	 */
	public static Class<?> getClassFirstGenericsParam(Class<?> clazz) {
		Type genericSuperclass = clazz.getGenericSuperclass();
		Type actualTypeArgument = ((ParameterizedTypeImpl) genericSuperclass).getActualTypeArguments()[0];
		return (Class<?>) actualTypeArgument;
	}

}