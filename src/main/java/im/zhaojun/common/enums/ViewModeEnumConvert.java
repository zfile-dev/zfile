package im.zhaojun.common.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ViewModeEnumConvert implements AttributeConverter<ViewModeEnum, String> {

    @Override
    public String convertToDatabaseColumn(ViewModeEnum attribute) {
        return attribute.value;
    }

    @Override
    public ViewModeEnum convertToEntityAttribute(String dbData) {
        return ViewModeEnum.getEnum(dbData);
    }

}