package im.zhaojun.common.model.enums;

import org.springframework.core.convert.converter.Converter;

public class StorageTypeEnumDeSerializerConvert implements Converter<String, StorageTypeEnum> {

    @Override
    public StorageTypeEnum convert(String s) {
        return StorageTypeEnum.getEnum(s);
    }
}
