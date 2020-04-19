package im.zhaojun.zfile.model.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * @author zhaojun
 */
public class StorageTypeEnumDeSerializerConvert implements Converter<String, StorageTypeEnum> {

    @Override
    public StorageTypeEnum convert(@NonNull String s) {
        return StorageTypeEnum.getEnum(s);
    }

}