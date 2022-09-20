package im.zhaojun.zfile.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Jackson 枚举反序列化器
 *
 * @author zhaojun
 */
@Slf4j
@Setter
@JsonComponent
public class JacksonEnumDeserializer extends JsonDeserializer<Enum<?>> implements ContextualDeserializer {

	private Class<?> clazz;


    /**
     * 反序列化操作
     *
     * @param   jsonParser
     *          json 解析器
     *
     * @param   ctx
     *          反序列化上下文
     *
     * @return  反序列化后的枚举值
     * @throws  IOException  反序列化异常
	 */
	@Override
	public Enum<?> deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
		Class<?> enumType = clazz;
		if (Objects.isNull(enumType) || !enumType.isEnum()) {
			return null;
		}
		String text = jsonParser.getText();
		Method method = StringToEnumConverterFactory.getMethod(clazz);
		Enum<?>[] enumConstants = (Enum<?>[]) enumType.getEnumConstants();

		// 将值与枚举对象对应并缓存
		for (Enum<?> e : enumConstants) {
			try {
				if (Objects.equals(method.invoke(e).toString(), text)) {
					return e;
				}
			} catch (IllegalAccessException | InvocationTargetException ex) {
				log.error("获取枚举值错误!!! ", ex);
			}
		}
		return null;
	}


	/**
	 * 为不同的枚举获取合适的解析器
	 *
	 * @param   ctx
     *          反序列化上下文
     *
	 * @param   property
     *          property
	 */
	@Override
	public JsonDeserializer<Enum<?>> createContextual(DeserializationContext ctx, BeanProperty property) {
		Class<?> rawCls = ctx.getContextualType().getRawClass();
		JacksonEnumDeserializer converter = new JacksonEnumDeserializer();
		converter.setClazz(rawCls);
		return converter;
	}

}