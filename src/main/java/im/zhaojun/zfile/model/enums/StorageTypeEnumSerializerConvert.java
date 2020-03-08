package im.zhaojun.zfile.model.enums;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author zhaojun
 */
public class StorageTypeEnumSerializerConvert extends JsonSerializer<StorageTypeEnum> {

    @Override
    public void serialize(StorageTypeEnum storageTypeEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(storageTypeEnum.getKey());
    }
}
