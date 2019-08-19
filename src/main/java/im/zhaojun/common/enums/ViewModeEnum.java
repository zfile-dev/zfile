package im.zhaojun.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ViewModeEnum {

    DETAILS("details"), ICONS("icons"), GRID("grid");

    private static Map<String, ViewModeEnum> enumMap = new HashMap<>();

    static {
        for (ViewModeEnum type : ViewModeEnum.values()) {
            enumMap.put(type.getValue(), type);
        }
    }

    String value;

    ViewModeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ViewModeEnum getEnum(String value) {
        return enumMap.get(value);
    }
}
