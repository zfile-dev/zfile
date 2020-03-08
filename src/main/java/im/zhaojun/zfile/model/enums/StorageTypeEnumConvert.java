package im.zhaojun.zfile.model.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author zhaojun
 */
@Converter(autoApply = true)
public class StorageTypeEnumConvert implements AttributeConverter<StorageTypeEnum, String> {

    @Override
    public String convertToDatabaseColumn(StorageTypeEnum attribute) {
        return attribute.getKey();
    }

    @Override
    public StorageTypeEnum convertToEntityAttribute(String dbData) {
        return StorageTypeEnum.getEnum(dbData);
    }

}