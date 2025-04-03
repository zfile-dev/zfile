package im.zhaojun.zfile.core.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;


/**
 * JSON String 序列化器, 用于将 JSON 字符串序列化为 JSON 对象.
 *
 * @author zhaojun
 */
public class JSONStringSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeRawValue(value);
    }

}